(ns khroma.tabs
  (:require [khroma.log :as console]
            [khroma.util :as kutil]
            [clojure.walk :as walk]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn get-tab
  "Returns a channel where we'll put a tab's information from its id"
  [tab-id]
  (let [ch (async/chan)]
    (.get js/chrome.tabs tab-id
      (fn [tab]
        (async/put! ch (walk/keywordize-keys (js->clj {:tab tab}))))) ch))

(defn get-active-tab
  "Returns a channel where we'll put the information for the current tab"
  []
  (let [ch (async/chan)]
    (.query js/chrome.tabs #js {:active true :currentWindow true}
      (fn [result]
        (when-let [tab (first result)]
          (async/put! ch (walk/keywordize-keys (js->clj {:tab tab})))))) ch))

(defn create
  "Creates a new tab with the specified properties"
  ([]
   (create {} nil))
  ([props]
   (create props nil))
  ([props callback]
   (.create js/chrome.tabs (clj->js props) callback)))


(defn on-created
  "Receives events when a tab is created."
  []
  (kutil/add-listener js/chrome.tabs.onCreated :tab))

(defn on-updated
  "Receives events when a tab is updated. This will include changing the URL,
  title or any content, not only creation. It will not fire when a tab is
  removed."
  []
  (kutil/add-listener js/chrome.tabs.onUpdated :tabId :changeInfo :tab))

(defn on-removed
  "Receives events when a tab is removed."
  []
  (kutil/add-listener js/chrome.tabs.onRemoved :tabId :removeInfo))

(defn on-replaced
  "Receives events when a tab is replaced with another tab. The notification
  will include the id for the tabs added and removed."
  []
  (kutil/add-listener js/chrome.tabs.onReplaced :added :removed))


(defn tab-created-events "DEPRECATED" []
  (kutil/deprecated on-created "tabs/on-created"))

(defn tab-updated-events "DEPRECATED" []
  (kutil/deprecated on-updated "tabs/on-updated"))

(defn tab-removed-events "DEPRECATED" []
  (kutil/deprecated on-removed "tabs/on-removed"))

(defn tab-replaced-events "DEPRECATED" []
  (kutil/deprecated on-replaced "tabs/on-replaced"))





