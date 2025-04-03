(ns replies 
  (:require
    [clojure.string :as s]))
(def baseurl "https://github.com/bitburner-official/bitburner-src/blob/dev/markdown/bitburner.")

(def strs
  {:ns {:spoil? false
        :url (str baseurl "ns.")
        :strs ["args" "alert" "asleep" "atExit" "brutessh" "clear" "clearLog" "clearPort"
               "closeTail" "deleteServer" "disableLog" "enableLog" "exec" "exit"
               "fileExists" "flags" "formatNumber" "formatPercent" "formatRam" "ftpcrack"
               "getBitnodeMultipliers" "getFavorToDonate" "getFunctionRamCost" "getGrowTime"
               "getHackingLevel" "getHackingMultipliers" "getHacknetMultipliers" "getHackTime"
               "getHostname" "getMoneySources" "getPlayer" "getPortHandle" "getPurchasedServerCost"
               "getPurchasedServerLimit" "getPurchasedServerMaxRam" "getPurchasedServers"
               "getpurchasedServerUpgradeCost" "getRecentScripts" "getResetInfo" "getRunningScript"
               "getScriptExpGain" "getScriptIncome" "getScriptLogs" "getScriptName" "getScriptRam"
               "getServer" "getServerBaseSecurityLevel" "getServerGrowth" "getServerMaxMoney"
               "getServerMaxRam" "getServerMinSecurityLevel" "getServerMoneyAvailable"
               "getServerNumPortsRequired" "getServerRequiredHackingLevel" "getServerSecurityLevel"
               "getServerUsedRam" "getSharePower" "getTimeSinceLastAug" "getTotalScriptExpGain"
               "getTotalScriptIncome" "getWeakenTime" "grow" "growthAnalyze" "growthAnalyzeSecurity"
               "hack" "hackAnalyze" "hackAnalyzeChance" "hackAnalyzeSecurity" "hackAnalyzeThreads"
               "hasRootAccess" "hasTorRouter" "httpworm" "isLogEnabled" "isRunning" "kill" "kill1"
               "killall" "ls" "moveTail" "mv" "nextPortwrite" "nformat" "nuke" "peek" "print" "printf"
               "printraw" "prompt" "ps" "purchaseServer" "ramOverride" "read" "readPort" "relaysmtp"
               "renamePurchasedServer" "resizeTail" "rm" "run" "scan" "scp" "scriptKill" "scriptRunning"
               "self" "serverExists" "setTitle" "share" "sleep" "spawn" "sprintf" "sqlinject"
               "tail" "tformat" "toast" "tprint" "tprintf" "tprintraw" "tryWritePort"
               "upgradePurchasedServer" "vsprintf" "weaken" "weakenAnalyze" "wget"
               "write" "writeport"]}

   :bladeburner {:spoil? true
                 :url baseurl

                 :strs ["bladeburner"
                        "bladeburner.getActionAutolevel"
                        "bladeburner.getActionCountRemaining"
                        "bladeburner.getActionCurrentLevel"
                        "bladeburner.getActionCurrentTime"
                        "bladeburner.getActionEstimatedSuccessChance"
                        "bladeburner.getActionMaxLevel"
                        "bladeburner.getActionRepGain"
                        "bladeburner.getActionSuccesses"
                        "bladeburner.getActionTime"
                        "bladeburner.getBlackOpNames"
                        "bladeburner.getBlackOpRank"
                        "bladeburner.getBonusTime"
                        "bladeburner.getCity"
                        "bladeburner.getCityChaos"
                        "bladeburner.getCityCommunities"
                        "bladeburner.getCityEstimatedPopulation"
                        "bladeburner.getContractNames"
                        "bladeburner.getCurrentAction"
                        "bladeburner.getGeneralActionNames"
                        "bladeburner.getNextBlackOp"
                        "bladeburner.getOperationNames"
                        "bladeburner.getRank"
                        "bladeburner.getSkillLevel"
                        "bladeburner.getSkillNames"
                        "bladeburner.getSkillPoints"
                        "bladeburner.getSkillUpgradeCost"
                        "bladeburner.getStamina"
                        "bladeburner.getTeamSize"
                        "bladeburner.inBladeburner"
                        "bladeburner.joinBladeburnerDivision"
                        "bladeburner.joinBladeburnerFaction"
                        "bladeburner.nextUpdate"
                        "bladeburner.setActionAutolevel"
                        "bladeburner.setActionLevel"
                        "bladeburner.setTeamSize"
                        "bladeburner.startAction"
                        "bladeburner.stopBladeburnerAction"
                        "bladeburner.switchCity"
                        "bladeburner.upgradeSkill"]}

   :codingcontract {:spoil? false
                    :url baseurl
                    :strs ["codingcontract"
                           "codingcontract.attempt"
                           "codingcontract.createDummyContract"
                           "codingcontract.getContract"
                           "codingcontract.getContractType"
                           "codingcontract.getContractTypes"
                           "codingcontract.getData"
                           "codingcontract.getDescription"
                           "codingcontract.getNumTriesRemaining"]}

   :corporation {:spoil? true
                 :url baseurl
                 :strs ["corporation"
                        "corporation.acceptInvestmentOffer"
                        "corporation.bribe"
                        "corporation.buyBackShares"
                        "corporation.canCreateCorporation"
                        "corporation.createCorporation"
                        "corporation.expandCity"
                        "corporation.expandIndustry"
                        "corporation.getBonusTime"
                        "corporation.getConstants"
                        "corporation.getCorporation"
                        "corporation.getDivision"
                        "corporation.getIndustryData"
                        "corporation.getInvestmentOffer"
                        "corporation.getMaterialData"
                        "corporation.getUnlockCost"
                        "corporation.getUpgradeLevel"
                        "corporation.getUpgradeLevelCost"
                        "corporation.goPublic"
                        "corporation.hasCorporation"
                        "corporation.hasUnlock"
                        "corporation.issueDividends"
                        "corporation.issueNewShares"
                        "corporation.levelUpgrade"
                        "corporation.nextUpdate"
                        "corporation.purchaseUnlock"
                        "corporation.sellDivision"
                        "corporation.sellShares"]}})


(def replies
(into {}
 (for [{spoil? :spoil? url :url strs :strs} (vals strs)]
   (reduce
    (fn [a st]
      (assoc
       a
       (keyword (s/lower-case st))
       { :spoil? spoil? :str st :url (str url (s/lower-case st) ".md")}))
    {}
    strs))))
