package com.example.domain.advisor

import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SkincareRoutine
import com.example.domain.model.SkincareStep
import com.example.domain.model.SleepSession

/**
 * 睡眠バイオリズムとオンデバイス肌測定値に基づく
 * パーソナライズド・スキンケア処方エンジン
 */
object SkincareAdvisor {

    fun generateRoutine(
        sleep: SleepSession?,
        skin: SkinAnalysisResult?
    ): SkincareRoutine {
        val sleepScore = sleep?.sleepScore ?: 80
        val deepSleepRatio = (sleep?.deepSleepPercent ?: 20)
        val texture = skin?.textureScore ?: 80
        val redness = skin?.rednessScore ?: 80
        val darkCircles = skin?.darkCirclesScore ?: 80

        val morningSteps = mutableListOf<SkincareStep>()
        val eveningSteps = mutableListOf<SkincareStep>()

        // 1. 朝のルーティン構築
        morningSteps.add(
            SkincareStep(
                stepNumber = 1,
                category = "洗顔",
                productType = if (redness < 75) "アミノ酸系弱酸性フォーム（ぬるま湯洗顔）" else "マイルドジェル洗顔料",
                reason = "皮脂膜を破壊せず、夜間の睡眠中に分泌された過酸化脂質のみを選択的にオフします。"
            )
        )

        morningSteps.add(
            SkincareStep(
                stepNumber = 2,
                category = "抗酸化・血行促進",
                productType = if (darkCircles < 75) "ナイアシンアミド + ビタミンK配合アイセラム" else "高浸透型ビタミンC誘導体セラム",
                reason = if (darkCircles < 75) {
                    "昨夜の血流うっ血により目元クマが顕著です。毛細血管を強化し滞留ヘモグロビンを流します。"
                } else {
                    "紫外線と大気汚染による酸化ストレスを防ぎ、キメの透明感を一日中キープします。"
                }
            )
        )

        morningSteps.add(
            SkincareStep(
                stepNumber = 3,
                category = "角層バリア保湿",
                productType = if (deepSleepRatio < 20) "ヒト型セラミド（EOP/NP/AP）高配合エマルジョン" else "ヒアルロン酸・パンテノール乳液",
                reason = "成長ホルモン分泌が少なかったためバリア機能が低下傾向。セラミドで角層間脂質を疑似修復します。"
            )
        )

        morningSteps.add(
            SkincareStep(
                stepNumber = 4,
                category = "UV防御",
                productType = "ノンケミカル（紫外線吸収剤フリー）SPF50+ PA++++ 日焼け止め",
                reason = "睡眠修復が追いついていない表皮細胞を紫外線ダメージから100%遮断します。"
            )
        )

        // 2. 夜のリカバリールーティン構築
        eveningSteps.add(
            SkincareStep(
                stepNumber = 1,
                category = "クレンジング",
                productType = "植物オイルベース低摩擦クレンジングオイル",
                reason = "摩擦レスでメイク・微粒子汚れをオフし、夜間ターンオーバーの阻害要因を取り除きます。"
            )
        )

        eveningSteps.add(
            SkincareStep(
                stepNumber = 2,
                category = "深層修復活性化",
                productType = if (texture < 78) "バクチオール（植物性レチノール代替）または低刺激純粋レチノール" else "EGF・FGFペプチド導入美容液",
                reason = "ノンレム深睡眠時の成長ホルモン放出タイミングと同期させ、真皮コラーゲン合成を最大化します。"
            )
        )

        eveningSteps.add(
            SkincareStep(
                stepNumber = 3,
                category = "密封・抗炎症",
                productType = if (redness < 78) "CICA（ツボクサエキス）+ マデカッソシド鎮静スリーピングマスク" else "スクワラン高保湿ナイトクリーム",
                reason = "睡眠中の水分蒸散（TEWL）を物理的に防ぎ、翌朝起床時のツヤとキメを保護します。"
            )
        )

        val summary = when {
            sleepScore >= 85 && texture >= 85 -> "理想的な睡眠修復が行われ、肌のバリア機能・キメともに極めて高水準です。過剰なケアを避け、抗酸化と保湿の維持を基本としてください。"
            deepSleepRatio < 20 || redness < 75 -> "昨夜の深層睡眠不足により、細胞修復の遅れと微細な炎症（赤み）の兆候が見られます。刺激の強い美容成分を控え、セラミド・CICAによる鎮静バリア補修を最優先してください。"
            darkCircles < 75 -> "レム睡眠および睡眠時間の不足により、眼輪筋周辺の微小循環が滞っています。ホットアイマスクやナイアシンアミドによる血流回復が有効です。"
            else -> "睡眠と肌は標準的なバランスを保っています。夜間のスキンケアで保湿膜を強化することで、今夜の睡眠修復効率をさらに底上げできます。"
        }

        val specialAdvice = if (deepSleepRatio < 20) {
            "【専門指導】成長ホルモン分泌が不足しています。今夜は就寝90分前に40℃の湯船に浸かり、深部体温の下降を誘導して深層睡眠比率20%超を目指しましょう。"
        } else {
            "【専門指導】良好な深層睡眠が取れています。朝一番にコップ1杯の常温水を補給し、夜間に失われた水分を細胞レベルで充填しましょう。"
        }

        return SkincareRoutine(
            title = if (skin != null) "今朝の生体肌処方箋（スコア: ${skin.overallScore}点）" else "睡眠予測型スキンケア処方箋",
            summary = summary,
            morningSteps = morningSteps,
            eveningSteps = eveningSteps,
            specialAdvice = specialAdvice
        )
    }
}
