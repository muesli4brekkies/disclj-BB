(ns routes
  (:gen-class)
  (:require
   [replies               :as r]
   [clojure.string :as string]))

(def empty-mdn
  {:condition (fn [msg event] (and (= "" msg) (re-matches #"(?i)^!mdn.*" (:content event))))
   :result (fn [_ _]  "https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Scripting")})

(def empty-ns
  {:condition (fn [msg event] (and (= "" msg) (re-matches #"(?i)^!ns.*" (:content event))))
   :result (fn [_ _]  "https://github.com/bitburner-official/bitburner-src/blob/stable/markdown/bitburner.md")})

(def too-long
  {:condition (fn [msg _] (< 70 (count msg)))
   :result (fn [_ _]  "Nuh uh, that request is too long.")})

(def robot
  {:condition (fn [msg _] (or (= msg "bleep bloop") (= msg "bloop bleep") (= msg "beep boop") (= msg "boop beep")))
   :result (fn [msg _] (string/join " " (-> msg (string/split #"\s") reverse)))})

(def spam
  {:condition (fn [msg _] (= msg "spam"))
   :result (fn [_ _] "https://media.tenor.com/_H10DMOzzzsAAAAM/spam.gif")})

(def yuri
  {:condition (fn [msg _] (= msg "yuri"))
   :result (fn [_ _] "https://muon.blog/botmedia/yuri.png")})

(def poast-coad
  {:condition (fn [msg _] (re-matches #"poast ?coad" msg))
   :result (fn [_ _] "https://muon.blog/botmedia/poastcoad.png")})

(def pspsps
  {:condition (fn [msg _] (re-find #"^ps(ps)+" msg))
   :result (fn [_ _] "https://muon.blog/botmedia/psps.png")})

(def zoe
  {:condition (fn [msg _] (or (= msg "zoe") (= msg "zoë")))
   :result (fn [_ _] "https://muon.blog/botmedia/zoe.jpg")})

(def code-format
  {:condition (fn [msg _] (re-matches #"code ?format" msg))
   :result (fn [_ _] "Please format your code so it is readable;
\\`\\`\\`js
like this
\\`\\`\\`
Those are backticks ( \\` ), not quotes ( ' ). Found to the left of the 1 key on many keyboards.
[​](https://muon.blog/botmedia/codeformat.png)")})

(def spoiler-format
  {:condition (fn [msg _] (re-matches #"spoiler ?format" msg))
   :result (fn [_ _] "Please add spoiler tags to spoily things;
\\|\\| your spoiler here \\|\\|
Those are pipe symbols ( | ). Found above the Enter key on many keyboards, and can be typed via [Shift + \\].")})

(def strike-format
  {:condition (fn [msg _] (re-matches #"strike([ -]?through)? ?format" msg))
   :result (fn [_ _] "To indicate outdated references/comments and avoid confusion, please strikethrough your text;
\\~\\~outdated comment\\~\\~
Those are tildes ( ~ ). Found to the left of the 1 key on many keyboards, and can be accessed via [Shift + \\`].")})

(def long-code
  {:condition (fn [msg _] (re-matches #"long ?code" msg))
   :result (fn [_ _] "https://muon.blog/botmedia/longcode.png")})

(def persecution
  {:condition (fn [msg _]
                (or
                 (seq (re-matches #".*(do( not|n'?t|nut|ughnut) buy hacknet).*" msg))
                 (seq (re-matches #".*(hacknet (is( not|n'?t) worth it|sucks|is a bad investment)).*" msg))))
   ;; Source: https://github.com/bitburner-official/typescript-template/issues/23
   :result (fn [_ _] "Hacknet a bad investment early game? You serious? Have you done your mathematics with that thing or not? I guess not. I'm from a heavily mathematical and scientific and scholastic family and lineage since like, Ancient China 5000 years ago? I was forced to do math drills before I could even play anything or whatever. I even had to compete in speed and accuracy with my childhood friend. Of course I won by a mile. Look, calculate the multiplicative aspects of the Hacknets. They'll earn way more than any early servers you can hack because the early game server are so darn poor. hack() is a percentage thingy as far as I can gather. Low server max money, low script income. It doesn't get any simpler to figure out than that. Before you insult my \"mental bandwidth\", I'll have you know that I have full score for all IQ tests I take anytime, anywhere, including those ever increasing difficulty ones that keep going until seemingly forever, and those were so easy and repetitive that I eventually got bored after I passed 300+IQ score and quit out of boredom, even though I originally was aiming for 1000+IQ score. I've always gotten 100% for my Mathematics from kindergarten till end of college/uni and so same programming subjects and logic and abstraction subject. Anyway, enough about me. Just wanted you to know not to judge other people without first getting to know them. It's rude.")})

(def duck
  {:condition (fn [msg _] (or (string/includes? msg "quack") (string/includes? msg "duck")))
   :result (fn [_ _] "quack 🦆")})

(def ipvgo
  {:condition (fn [msg _] (= msg "ipvgo"))
  :result (fn [_ _] "-# Originally written by FicocelliGuy:
Your goal is to build \"walls\" of touching pieces that surround empty space. If you make those connected walls surround two different \"rooms\", it is much harder or impossible for them to be captured.

At the same time you want to take away empty space from the areas your opponent is building in, with careful use of your pieces.

To think about it another way: You are trying to surround gardens with walls to protect them - but you can't grow anything if the open space for gardening gets completely paved over.

If you make a group of touching stones that surrounds two different empty spaces, they can't ever be taken, unless you pave over one of the spaces with your pieces.

For example: If you make a figure 8 or another continuous shape that surrounds more than one empty space, it is immortal - the opponent can't capture it anymore, unless you wall over one of those spaces yourself. (This is because the opponent can only play one move at a time and can't play moves that would commit suicide)

If you are a visual learner, you can watch this playlist on the board game Go (which is what IPvGO is based upon) to learn the basics:
https://youtube.com/playlist?list=PL4DLlaT_bvDG5y6WSfXU8cQsTsb4o3YnT")})

(def endgame-welcome
  {:condition (fn [msg _] (or (re-matches #"endgame( welcome)?" msg) (= msg "welcome") (= msg "welcome to endgame")))
   :result (fn [_ event] (if (r/spoil-ok? event) "-# Originally written by FicocelliGuy:
Congratulations on beating the tutorial and welcome to endgame!

There are pins in this channel with recommendations for BitNodes to do early (repeating BitNode 1 to get Source File 1.2 is the strongest bonus, but BitNode 2 unlocks a new mechanic, and BitNode 5 unlocks some nice QoL).

You can change your mind and switch BitNodes at any time if you want, too! There's a new program you unlock to do that, called \"b1t_flum3.exe\"

If you have any questions, feel free to ask!" "Run this command in <#415207923506216971> to welcome the new person who has learned The Truth™!"))})

(def naughty
  {:condition (fn [msg event]
                (and (not (r/spoil-ok? event)) (some (fn [r] (string/includes? msg r)) r/spoilers)))
   :result (fn [_ _] r/tell-off)})

(def lookup
  {:condition (fn [_ _] true)
   :result (fn [msg event]
             (let [replies (if (string/starts-with? (:content event) "!ns") r/ns-replies r/mdn-replies)]
              (prn r/ns-replies)
               (r/signature-decorator
                msg
                event
                (fn [msg event]
                  (if-let [match (-> msg keyword replies :url)]
                    (str "<" match ">")
                    (r/fuzzy-search (r/spoil-ok? event) msg replies))))))})