(ns s3-cljs.core
  (:require [cljs.core.async :refer [<! chan put!]]
            [clojure.string :as string])
  (:use-macros 
    [cljs.core.async.macros :only [go go-loop]]
    [purnam.core :only [? ! obj !>]]))

(def s3)

(defn config [credentials]
  (!> js/AWS.config.update (clj->js credentials)))

(defn init [credentials]
  (set! s3 (let [constructor (? js/AWS.S3)]
             (config credentials)
             (constructor.))))

(defn body-data-in-str [data]
  (-> (:Body data)
      str
      string/triml))

(defn getSignedUrl 
  ([operation params] (!> s3.getSignedUrl operation (clj->js params)))
  ([operation params callback] (!> s3.getSignedUrl operation (clj->js params) callback)))

(defn listObjects [params callback]
  (!> s3.listObjects (clj->js params) callback))

(defn getObject [params callback]
  (!> s3.getObject (clj->js params) callback))

(defn get-object [bucket url]
  (let [c (chan)]
    (getObject {:Bucket bucket
                   ;:ContentType "text/plain"
                   :Key url}
                  (fn [err data]
                    (put! c [url (js->clj data :keywordize-keys true)])))
    c))

(defn get-object-body [bucket url]
  (let [c (chan)]
    (getObject {:Bucket bucket
                   :Key url}
                  (fn [err data]
                    (if err
                      (put! c [:error err])
                      (put! c [:ok (body-data-in-str data)]))))
    c))

(defn list-objects 
  ([params pred] (let [c (chan)]
                   (listObjects params
                                (fn [err data]
                                  (if err
                                    (put! c [:error err])
                                    (let [contents (js->clj (? data.Contents) :keywordize-keys true)
                                          files (map :Key contents)
                                          filtered-files (filter pred files)]
                                      (put! c [:ok filtered-files])))))
                   c))
  ([params] (list-objects params (constantly true))))

(defn files-with-prefix-and-suffix [bucket prefix regex]
  (go
    (let [[status data] (<! (list-objects
                              {:Bucket bucket
                               :MaxKeys 1000
                               :Prefix prefix}
                              regex))]
      (case status
        :error []
        :ok data))))


(defn get-several-files [get-single-file-func coll]
  (go-loop [res {} channels (map get-single-file-func coll)]
           (if (empty? channels)
             res
             (let [[[file data] c] (alts! channels)]
               (recur (assoc res file data) (remove #{c} channels))))))

(defn get-objects [params pred]
  (go
    (let [[status data] (<! (list-objects params pred))]
      (case status
        :error [status data]
        :ok (let [meta-data (<! (get-several-files (partial get-object (:Bucket params)) data))]
              [status meta-data])))))
