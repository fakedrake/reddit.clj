(ns reddit.clj.core
  "Reddit client for clojure"
  (:require [reddit.clj.client :as client])
)

(defprotocol RedditChannels
  "The reddit web API interfaces for reading data from reddit"
  (reddits 
    [this rname] [this rname rcount after]
    "Retrieve reddits from subreddit")
  (user 
    [this user] [this user qualifier] [this user qualifier rcount after]
    "Retrieve reddits related by user")
  (about
    [this user]
    "Retrieve user information")
  (comments 
    [this reddit-id] 
    "Retrieve comments for a reddit")
  (domain 
    [this domain-name] [this domain-name rcount after]
    "Retrieve reddits under a domain")
  (saved 
    [this] [this rcount after]
    "Retrieve saved reddits")
  (info 
    [this url] 
    "Retrieve url information from reddit")
  (me 
    [this] 
    "Retrieve user information according to current credential")
  (mine 
    [this] 
    "Retrieve subcribed subreddits according to current credential "))
 
(defprotocol RedditOperations  
  "The reddit web API interfaces for writing data into reddit"
  (vote-up
    [this id]
    "Vote up a comment or post")
  (vote-down
    [this id]
    "Vote down a comment or post")
  (rescind-vote
    [this id]
    "Rescind vote to a comment or post")
  (add-comment
    [this id text]
    "Comment on a post or comment")
  (save
    [this id]
    "Add a post to your saved reddits")
  (unsave
    [this id]
    "Remove a post from your saved reddits")
  (submit-link
    [this title url sr]
    "Submit a link to particular subreddit")
  (submit-text
    [this title text sr]
    "Submit a self post to particular subreddit")
  (hide
    [this id]
    "Hide a post")
  (unhide
    [this id]
    "Unhide a post"))

(defrecord RedditClient [credential]
  RedditChannels
    (reddits [this rname] 
      (client/subreddit rname credential nil nil))
    (reddits [this rname rcount after] 
      (client/subreddit rname credential rcount after))
    (user [this user] 
      (client/userreddit user credential nil nil nil))
    (user [this user qualifier] 
      (client/userreddit user credential qualifier nil nil))
    (user [this user qualifier rcount after] 
      (client/userreddit user credential qualifier rcount after))
    (comments [this reddit-id] 
      (client/redditcomments reddit-id credential))
    (saved [this] 
      (client/savedreddits credential nil nil))
    (saved [this rcount after]
      (client/savedreddits credential rcount after))
    (domain [this domain-name] 
      (client/domainreddits domain-name credential nil nil))
    (domain [this domain-name rcount after] 
      (client/domainreddits domain-name credential rcount after))
    (info [this url] 
      (client/info url credential))
    (mine [this]
      (client/mine credential))
    (me [this]
      (client/me credential)))


(defn login "Login to reddit, return cookie as user credential"
  ([] (RedditClient. nil))
  ([user passwd]
  (if (nil? user) (RedditClient. nil)
    (RedditClient. (client/login user passwd)))))

(defn enhance [r]
  (assoc r :modhash (:modhash (me r))))
  
(extend-type RedditClient
  RedditOperations
    (vote-up [this id] 
      (client/vote id "1" (:modhash this) (:credential this)))
    (vote-down [this id] 
      (client/vote id "-1" (:modhash this) (:credential this)))
    (rescind-vote [this id] 
      (client/vote id "0" (:modhash this) (:credential this)))
    (add-comment [this id text] 
      (client/add-comment id text (:modhash this) (:credential this)))
    (save [this id]
      (client/save id (:modhash this) (:credential this)))
    (unsave [this id] 
      (client/unsave id (:modhash this) (:credential this)))
    (submit-link [this title url sr] 
      (client/submit "link" title sr url  (:modhash this) (:credential this)))
    (submit-text [this title text sr] 
      (client/submit "text" title sr text (:modhash this) (:credential this)))
    (hide [this id]
      (client/hide id  (:modhash this) (:credential this)))
    (unhide [this id]
      (client/unhide id  (:modhash this) (:credential this))))

