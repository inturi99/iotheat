(ns iotheat.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as ring-json]
            [ring.util.response	:as rr]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.cors :refer [wrap-cors]]
            [iotheat.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [cheshire.core :refer :all])
  (:use org.httpkit.server)
  (:gen-class))

(defn validate-device-heat [params]
  (first (b/validate
          params
          "uid" v/required
          "heat"[v/required v/number]
          "uv" [v/required v/number])))

(def content-type  "application/json; charset=utf-8")

(def clients (atom {}))

(defn handler [request]
  (with-channel request channel
    (swap! clients assoc channel true)
    (on-close channel (fn [status] (println "client close it" status)))
    (on-receive channel (fn [data]
                          (send! channel "registered")))))

(defn is-same-temparature? [uid temperature]
  (let [latestrec (first (db/get-deviceheat-by-uid-latest {:uid uid}))]
    (and (not (nil? latestrec))
         (= (int (* 10 (:temperature latestrec))) (int (* 10 temperature))))))

(defn push-data [clnts data]
  (doseq [client (keys @clnts)]
    (send! client data)))

(defroutes app-routes
  (GET "/deviceheat/latest/:uid" [uid] (rr/content-type
                                        (rr/response  (db/get-deviceheat-by-uid-latest
                                                       {:uid uid}))
                                        content-type))
  (GET "/deviceheat/all/:uid" [uid] (rr/content-type
                                     (rr/response  (db/get-uid-all-deviceheat
                                                    {:uid uid}))
                                     content-type))
  (GET "/deviceheat1/:uid/:temperature/:uv" [uid temperature uv]
       (db/insert-deviceheat {:uid uid
                              :temperature (if (nil? temperature)-1 (read-string temperature))
                              :uv (if (nil? uv)-1 (read-string uv))})
       (rr/content-type
        (rr/response "") content-type))

  (GET "/deviceheat" [] (rr/content-type
                         (rr/response  (db/get-all-deviceheat-latest))
                         content-type))
  (GET "/deviceheat/check/:uid/:temp" [uid temp]
       (rr/content-type
        (rr/response {:val 20}) content-type))

  (GET "/ws" [] handler)

  (POST "/deviceheat" {body :body}
        (let [{t "temperature" uid "uid"
               uv "uv"} body]
          (when-not (is-same-temparature? uid t)
            (db/insert-deviceheat
             {:uid uid
              :temperature (if (nil? t)-1 t)
              :uv (if (nil? uv)-1 uv)})
            (push-data clients (generate-string (db/get-all-deviceheat-latest))))
          (rr/content-type
           (rr/response "") content-type)))
  (route/not-found "<h1>Page not found</h1>"))


(def app
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      (ring-json/wrap-json-body)
      (ring-json/wrap-json-response)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run-server app {:port 8192
                   :join? false}))
