# STATIC RAM PARSING

## What it is
Static RAM is the total amount of RAM the game *pre-assigns* to the script before it is executed. For example, if you call `ns.hack()`, the game adds the `hack` function's RAM cost to the script. Every time you call a NetScript function, the game re-checks the static RAM cost and updates it accordingly.

Note that creating a script incurs a 1.60GB RAM cost by default. This is unavoidable.

## Problems
To see the most common problem with static RAM parsing, let's create a script that hacks n00dles and prints the amount of money it stole to the terminal.

Replace the contents of your newly-made test script with the below code:
```js
/** @param {NS} ns */
export async function main(ns) {
  ns.tprint(await ns.hack("n00dles"));
}
```
As you can see, the RAM cost in the in-game editor is now 1.70GB. This is correct, the `hack` function's RAM cost is 0.10GB, and, added to the base cost of the script (which is 1.60GB, as stated in the previous section), it does indeed sum to 1.70GB.

Now, to store the money we hacked, let's store it in a variable. Name it `stolenMoney`:
```js
  let stolenMoney = await ns.hack("n00dles");
  ns.tprint(stolenMoney);
```
Re-check the static RAM cost of the script. It is still the same cost, 1.70GB, which is correct.

Now, to see the real problem: We know that a `hack` can be unsuccessful (which can possibly happen when the hacking chance on the server is less than 1, and definitely happens if the server's security is maximum [100] and/or the server's available money is minimum [0]). Therefore, we should name the variable accordingly.

Since we are attempting to hack, let us rename `stolenMoney` to `attempt`:
```js
  // Attempt to hack the n00dles server
  let attempt = await ns.hack("n00dles");
  ns.tprint(attempt);
```
Now, re-check the details of the static RAM cost of the script.

We have the base RAM cost of the script (1.60GB), the `hack` function's cost (0.10GB), and a new `codingcontract.attempt` cost, which is 10GB!

But we didn't call this API, so what happened?

## Why this happened
The static RAM parser is good at what it does - check for NetScript functions and apply the cumulative determined RAM cost to the static RAM cost of the script accordingly. But sometimes, it gives false positives, such as the one we tested, among others.

The keyword `attempt` is part of the `codingcontract` API. The static RAM parser *thinks* we called the API, so it applies the 10GB RAM cost.

But we didn't! We just named a variable!

## Workarounds
The most obvious workaround, as said by hydroflame, is:
> ...that sounds like a lot of effort for very little reward. Name your variables something else.

[Original Message](https://discord.com/channels/415207508303544321/415213413745164318/899511549557899295)

Note that while `attempt` incurs the API cost, `hackAttempt` doesn't. Thus, adding a prefix/suffix to your variables may be enough for you to avoid it.

## But... how do I know the API keywords?
It may be tedious to check all keywords on a blank script and check the static RAM every time.

Instead, you just need to check whether your variable name turns [blue](https://muon.blog/botmedia/blue-keywords.png) in the in-game editor (or whatever color your syntax.markup is in the Editor Theme).

## And what if I need this keyword?
d0sboots added a specific NetScript function for that: `ns.ramOverride`. If you add it at the beginning of the `main(ns)` function and give it a number (say, 1.70, alluding to our previous example of using `attempt`), it will override the static RAM checker with the parameter as the total RAM cost of the script.

However, a script gains a cumulative dynamic RAM of the functions it has run so far, and if this dynamic RAM exceeds the static RAM cost of the script, the script will throw an error and stop execution. So, to avoid the error, you have to include the base RAM cost of the script (1.60GB) and the RAM cost of all used NetScript functions, and also update the RAM override whenever you introduce a new function to the script that costs RAM.

## TLDR
Don't use NetScript API keywords to name your own variables.