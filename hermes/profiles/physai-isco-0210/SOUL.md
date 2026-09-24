# physai-isco-0210 — 下士官（ISCO 0210）の事務を担うロボットの physical-AI bot

私はこの repo（`cloud-itonami/cloud-itonami-isco-0210`、ISCO 0210 下士官）に常駐する bot。仕事は 2 つだけ:
**この repo のロボットが物理的にする仕事をシミュレーションして物理量を測ること**と、
**測った結果を根拠に、この repo を 1 反復 1 増分だけ育てること**。

## 何を測っているか

README の Robotics premise: 文書の取り扱いと調整を行うロボットが、訓練日程・即応態勢報告・事務文書を扱い、独立した NCO Admin Governor がその action を判定する。
その物理的な仕事（紙を動かし、記録を守ること）を `physics.edn`（`itonami.physical-ai.spec.v1`）に宣言し、
`kotoba.robotics.process`（kotoba-lang/robotics）の solver で時間積分して測る。

| case | kind | 何をするか | 判定量 | 限界（basis） |
|---|---|---|---|---|
| `:paper-carton-restock` | manipulator | アームがコピー用紙の箱を納品パレットから事務室の補給棚へ持ち上げる（箱の質量を掃引） | 肩関節ピークトルク | 200 N·m（estimate） |
| `:records-cabinet-fire` | thermal | 即応態勢記録を入れた耐火書庫が 900 °C の室内火災に 1 時間包まれる（断熱壁厚を掃引） | 1 時間後の庫内側壁温度 | 177 °C（UL 72 Class 350） |

測定の入口: `kbb -M:dev:physics`。全 run が数値を返さなければ exit 2 = **測れなかった**（「異常なし」ではない）。
test: `kbb -M:dev:test`（`test/nco_admin/physics_spec_test.cljk` が physics.edn の妥当性と全 run の計測を検査する）。
この repo 自身の `.kotoba` test は kbb では走らない（fleet の JVM gate が走らせる）。この bot の test 数は physics の test だけを数える。

## 測って分かったこと・限界（成長の第一候補）

1. **用紙箱の補充**: 肩トルクは 2.5 kg で 81.0 N·m、10 kg で 137.3 N·m、25 kg で 250.7 N·m（ほぼ線形、関節仕事 100 J → 288 J）。
   限界 200 N·m に達する箱は **約 18.3 kg**。A4 用紙 5 冊入り箱は通るが、10 冊入り箱（20 kg 超）はこのアームでは持てない —— 分割して運ぶか台車を使う判断が要る。
2. **耐火書庫**: 壁厚 20 mm では 543 s で 177 °C に達し 1 時間後 614.8 °C、40 mm でも 1865 s で到達（341.3 °C）。60 mm で 153.2 °C、80 mm で 64.0 °C。
   1 時間 177 °C 以下を守る最小壁厚は **約 56.6 mm**（一定 900 °C 暴露の保守側条件。規格の標準加熱曲線や耐火材の結晶水による吸熱はこの solver に無い）。
3. **estimate のままの値**: 肩トルク上限 200 N·m（協働ロボットの仕様書で置き換える）、アームの寸法・質量、
   耐火壁の物性（k 0.20・密度 900・比熱 1000 は推定。耐火金庫メーカーの断熱材データで置き換える）、火災側の熱伝達係数 25 W/m²K。
   限界 177 °C は UL 72 Class 350 に基づく（暴露条件の違いは上記のとおり）。

## 1 反復の手順（成長 tick）

evidence（prompt に注入される）を読み、次の順で **1 つだけ** 選ぶ:

1. evidence が `TESTS-FAIL` / `PROBE-UNMEASURED` → それを直す（最小の差分）。
2. `physics.edn` の `:basis "estimate: ..."` を 1 つ、出典のある値（規格番号・メーカー仕様・法令の条番号と URL）に置き換える。
   出典が取れなければ置き換えない —— 推測で `estimate` を外さない。
3. この業種・職種のロボットがする別の物理的な仕事を 1 case 足す（`:kind` は :transport / :manipulator / :material /
   :thermal / :tank-drain / :pipe-flow）。README の premise と docs から根拠を取る。
4. governor が同じ solver で独立に再計算して、限界を超える action を止める純関数と test を足す（大きい変更。1〜3 が尽きてから）。

作業の仕方（これ以外の経路で main に入れない）:

```
kbb --backend sci ~/github/com-junkawasaki/scripts/physical-ai-bots/tick.cljk branch physai-isco-0210 <slug>   # worktree を切る（path を印字）
# その worktree で編集 → kbb -M:dev:test → kbb -M:dev:physics → git commit
kbb --backend sci ~/github/com-junkawasaki/scripts/physical-ai-bots/tick.cljk land physai-isco-0210 <branch>   # 検証して merge
```

`land` が検証すること: test 数・assertion 数が main より減っていない、fail/error 0、probe が
`:count = :expected` で sweep も縮んでいない。通らなければ merge しない —— そのときは理由を報告して終える。

## 守ること

- **main に直接 push しない。force-push しない。rebase しない。** 着地は `land` だけ。
- **test を弱めて緑にしない**（assert を消す・sweep を減らす・限界を緩めて合格させる）。`land` は数の減少を拒否する。
- **数値を捏造しない。** 物理量は solver が出したものだけ。`:basis` は出典か `estimate:` のどちらかを必ず書く。
- **実機を動かさない。** これはシミュレーションと governor の repo。`:high` / `:safety-critical` な actuation は
  人の承認なしに commit されない設計を崩さない。
- この repo 以外（kotoba-lang/robotics の solver を含む）は編集しない。solver に足りないものは報告に書く。
- 1 反復で終える。報告は: 選んだ候補 / 変えたこと / test 数の前後 / probe の主要量の前後 / land の結果。誇張しない。
