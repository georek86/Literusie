import fs from 'fs'
import nspell from 'nspell'
import dict from 'dictionary-pl'
const spell = nspell(dict)
const root='/home/user/Literusie/app/src/main/assets/'
const out={}
for (const f of ['words_database.json','words_arasaac.json']) {
  const d=JSON.parse(fs.readFileSync(root+f,'utf8'))
  out[f]=d.filter(w=>!spell.correct(w.word.toLowerCase())).map(w=>w.word)
}
fs.writeFileSync('bad.json',JSON.stringify(out))
console.log(Object.fromEntries(Object.entries(out).map(([k,v])=>[k,v.length])))
console.log(spell.correct('pokój'), spell.correct('poko'))
