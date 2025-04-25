#! deno -A
import { ZipReader } from "jsr:@zip-js/zip-js";
const MDN_REPO_URL = 'https://codeload.github.com/mdn/content/zip/refs/heads/main';

const reader = await fetch(MDN_REPO_URL)
  .then(r => (new ZipReader(r.body!)));

const js_ref = (await reader.getEntries())
  .filter(entry => entry.filename.startsWith("content-main/files/en-us/web/javascript/reference"))
  .filter(entry => entry.filename.endsWith("/index.md"))
  .map(entry => [
    entry.filename.match(/^.*?javascript\/reference\/(.*?)\/index.md$/)?.[1],
    entry.filename.replace(/^.*?\/en-us/, 'https://developer.mozilla.org/en-US/docs').replace(/\/index\.md/, "")
  ])
  .map(([key, value]) => key?.startsWith("global_objects") ? [
    key?.replace(/^global_objects\//, "").replaceAll("/", ".")
    , value
  ] : [key?.replaceAll("/", " "), value])
  .filter(([a]) => a != undefined)
  .reduce((prev, [key, value]) => `${prev}\n${key}=${value}`, '');

Deno.writeTextFile("./mdn-ref.properties", js_ref);