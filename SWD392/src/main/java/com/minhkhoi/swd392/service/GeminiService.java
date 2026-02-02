package com.minhkhoi.swd392.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiService {

    @org.springframework.beans.factory.annotation.Value("${gemini.api-key}")
    private String apiKey;
    // Sửa thành phiên bản mới nhất hiển thị trên màn hình của bạn
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.0-flash:generateContent";
    /**
     * Generate quiz questions from content using Google Gemini.
     *
     * @param content The video transcript or document content.
     * @return JSON string containing the generated questions
     */
    public String generateQuizQuestions(String content) {



        String mockJapaneseQuiz = """
    [
      {
        "content": "「冷蔵庫」の意味として正しいものはどれですか？",
        "options": {
          "A": "Lò vi sóng",
          "B": "Tủ lạnh",
          "C": "Máy giặt",
          "D": "Điều hòa"
        },
        "correctAnswer": "B",
        "explanation": "「冷蔵庫（れいぞうこ）」は食品を冷やして保存する「Tủ lạnh」のことです。"
      },
      {
        "content": "「Giáo viên」は日本語で何と言いますか？",
        "options": {
          "A": "がくせい (Gakusei)",
          "B": "いしゃ (Isha)",
          "C": "きょうし (Kyoushi)",
          "D": "かいしゃいん (Kaishain)"
        },
        "correctAnswer": "C",
        "explanation": "「Giáo viên」は日本語で「教師（きょうし）」または「先生（せんせい）」と言います。"
      },
      {
        "content": "「学生」の正しい読み方はどれですか？",
        "options": {
          "A": "がくせ",
          "B": "がっせい",
          "C": "がくせい",
          "D": "かせい"
        },
        "correctAnswer": "C",
        "explanation": "「学生」は「がくせい」と読みます。長音（ー）が含まれない表記に注意しましょう（発音は gakusee に近いです）。"
      },
      {
        "content": "「せんせい」を漢字で書くとどうなりますか？",
        "options": {
          "A": "先正",
          "B": "先生",
          "C": "先世",
          "D": "前生"
        },
        "correctAnswer": "B",
        "explanation": "「せんせい」の正しい漢字は「先生」です。"
      },
      {
        "content": "ごはんを（　　）で食べます。",
        "options": {
          "A": "はし",
          "B": "コップ",
          "C": "電子レンジ",
          "D": "冷蔵庫"
        },
        "correctAnswer": "A",
        "explanation": "文脈から食事に使う道具を選びます。「はし（Đũa）」が正解です。"
      },
      {
        "content": "「洗います」の意味は何ですか？",
        "options": {
          "A": "Dùng / Sử dụng",
          "B": "Giặt / Rửa",
          "C": "Cắt / Gọt",
          "D": "Đặt / Để"
        },
        "correctAnswer": "B",
        "explanation": "「洗います（あらいます）」は「Giặt (quần áo)」や「Rửa (tay, bát đĩa)」という意味です。"
      },
      {
        "content": "「Viết」を表す動詞はどれですか？",
        "options": {
          "A": "聞きます (Kikimasu)",
          "B": "書きます (Kakimasu)",
          "C": "置きます (Okimasu)",
          "D": "貸します (Kashimasu)"
        },
        "correctAnswer": "B",
        "explanation": "「Viết」は日本語で「書きます（かきます）」です。"
      },
      {
        "content": "「韓国」の読み方はどれですか？",
        "options": {
          "A": "ちゅうごく",
          "B": "かんこく",
          "C": "にほん",
          "D": "がいこく"
        },
        "correctAnswer": "B",
        "explanation": "「韓国」は「かんこく」と読みます。「ちゅうごく」は中国のことです。"
      },
      {
        "content": "ナイフで肉を（　　）。",
        "options": {
          "A": "切ります",
          "B": "手伝います",
          "C": "貸します",
          "D": "聞きます"
        },
        "correctAnswer": "A",
        "explanation": "ナイフ（Dao）を使う動作は「切ります（Cắt）」が適切です。"
      },
      {
        "content": "「会社員」の意味として正しいものはどれですか？",
        "options": {
          "A": "Học sinh",
          "B": "Bác sĩ",
          "C": "Nhân viên công ty",
          "D": "Giáo viên tiếng Nhật"
        },
        "correctAnswer": "C",
        "explanation": "「会社員（かいしゃいん）」は企業で働く「Nhân viên công ty」のことです。"
      },
      {
        "content": "「Muối」は日本語で何と言いますか？",
        "options": {
          "A": "さとう (Satou)",
          "B": "しょうゆ (Shouyu)",
          "C": "しお (Shio)",
          "D": "あじ (Aji)"
        },
        "correctAnswer": "C",
        "explanation": "「Muối」は「塩（しお）」です。「さとう」は砂糖（Đường）です。"
      },
      {
        "content": "「大学」の読み方はどれですか？",
        "options": {
          "A": "だいがっこう",
          "B": "だいがく",
          "C": "たいがく",
          "D": "おおがく"
        },
        "correctAnswer": "B",
        "explanation": "「大学」は「だいがく」と読みます。"
      },
      {
        "content": "「借ります」の反対の意味（Cho mượn）を表す言葉はどれですか？",
        "options": {
          "A": "使います",
          "B": "取ります",
          "C": "手伝います",
          "D": "貸します"
        },
        "correctAnswer": "D",
        "explanation": "「Cho mượn」は「貸します（かします）」です。"
      },
      {
        "content": "「にほん」を漢字で書くとどうなりますか？",
        "options": {
          "A": "日本",
          "B": "日木",
          "C": "旦本",
          "D": "入本"
        },
        "correctAnswer": "A",
        "explanation": "「にほん」は「日本」と書きます。"
      },
      {
        "content": "「電子レンジ」は何に使いますか？",
        "options": {
          "A": "服を洗う",
          "B": "食べ物を温める",
          "C": "部屋を掃除する",
          "D": "音楽を聴く"
        },
        "correctAnswer": "B",
        "explanation": "「電子レンジ（Lò vi sóng）」は食べ物を温める（hâm nóng）ために使います。"
      },
      {
        "content": "「手伝います」の意味は何ですか？",
        "options": {
          "A": "Giúp đỡ",
          "B": "Sử dụng",
          "C": "Cầm / Lấy",
          "D": "Rửa"
        },
        "correctAnswer": "A",
        "explanation": "「手伝います（てつだいます）」は「Giúp đỡ」という意味です。"
      },
      {
        "content": "「Người Mỹ」は日本語で何と言いますか？",
        "options": {
          "A": "アメリカ語",
          "B": "アメリカ人",
          "C": "アメリカ方",
          "D": "アメリカ員"
        },
        "correctAnswer": "B",
        "explanation": "国名 ＋ 人（じん）で国籍を表します。「アメリカ人」が正解です。"
      },
      {
        "content": "「中国」の読み方はどれですか？",
        "options": {
          "A": "ちゅうごく",
          "B": "ちゅごく",
          "C": "なかぐに",
          "D": "ちゅうこく"
        },
        "correctAnswer": "A",
        "explanation": "「中国」は「ちゅうごく」と読みます。"
      },
      {
        "content": "先生に質問を（　　）。",
        "options": {
          "A": "聞きます",
          "B": "置きます",
          "C": "洗います",
          "D": "切ります"
        },
        "correctAnswer": "A",
        "explanation": "質問（しつもん）を「Hỏi」場合は「聞きます（ききます）」を使います。"
      },
      {
        "content": "「砂糖」の意味として正しいものはどれですか？",
        "options": {
          "A": "Muối",
          "B": "Đường",
          "C": "Nước mắm",
          "D": "Hạt tiêu"
        },
        "correctAnswer": "B",
        "explanation": "「砂糖（さとう）」は甘い調味料の「Đường」です。"
      },
      {
        "content": "「Cốc / Cái ly」は日本語で何と言いますか？",
        "options": {
          "A": "テーブル",
          "B": "スプーン",
          "C": "コップ",
          "D": "ナイフ"
        },
        "correctAnswer": "C",
        "explanation": "「Cốc」は英語の Cup から「コップ」と言います。"
      },
      {
        "content": "「高校」の読み方はどれですか？",
        "options": {
          "A": "こうこ",
          "B": "こうこう",
          "C": "ここう",
          "D": "こうごう"
        },
        "correctAnswer": "B",
        "explanation": "「高校（Trường cấp 3）」は「こうこう」と読みます。長音に注意しましょう。"
      },
      {
        "content": "机の上に本を（　　）。",
        "options": {
          "A": "置きます",
          "B": "洗います",
          "C": "手伝います",
          "D": "聞きます"
        },
        "correctAnswer": "A",
        "explanation": "「Đặt / Để」という意味の「置きます（おきます）」が適切です。"
      },
      {
        "content": "「わたし」を漢字で書くとどうなりますか？",
        "options": {
          "A": "私",
          "B": "仏",
          "C": "利",
          "D": "和"
        },
        "correctAnswer": "A",
        "explanation": "「わたし（Tôi）」の漢字は「私」です。"
      },
      {
        "content": "「使います」の意味は何ですか？",
        "options": {
          "A": "Mua",
          "B": "Bán",
          "C": "Sử dụng",
          "D": "Làm"
        },
        "correctAnswer": "C",
        "explanation": "「使います（つかいます）」は「Sử dụng」または「Dùng」という意味です。"
      },
      {
        "content": "「Tên (của bạn)」は日本語で何と言いますか？",
        "options": {
          "A": "おくに",
          "B": "おしごと",
          "C": "おなまえ",
          "D": "おかね"
        },
        "correctAnswer": "C",
        "explanation": "「Tên」は「名前（なまえ）」です。丁寧にお聞きする場合は「お名前」と言います。"
      },
      {
        "content": "「名前」の読み方はどれですか？",
        "options": {
          "A": "なまえ",
          "B": "めいぜん",
          "C": "なまい",
          "D": "めいえ"
        },
        "correctAnswer": "A",
        "explanation": "「名前」は「なまえ」と読みます。"
      },
      {
        "content": "スプーンと（　　）でパスタを食べます。",
        "options": {
          "A": "フォーク",
          "B": "ナイフ",
          "C": "電子レンジ",
          "D": "冷蔵庫"
        },
        "correctAnswer": "A",
        "explanation": "パスタを食べる時にスプーンと一緒に使うのは「フォーク（Dĩa）」です。"
      },
      {
        "content": "「取ります」の意味は何ですか？",
        "options": {
          "A": "Chụp (ảnh) / Lấy / Cầm",
          "B": "Nghe / Hỏi",
          "C": "Viết / Vẽ",
          "D": "Cắt / Gọt"
        },
        "correctAnswer": "A",
        "explanation": "「取ります（とります）」は物を「Lấy/Cầm」や、写真を「Chụp」という意味があります。"
      },
      {
        "content": "「仕事」の読み方はどれですか？",
        "options": {
          "A": "しごと",
          "B": "しかた",
          "C": "しじ",
          "D": "よごと"
        },
        "correctAnswer": "A",
        "explanation": "「仕事（Công việc）」は「しごと」と読みます。"
      }
    ]
    """;
        return mockJapaneseQuiz.trim();
    }

    private String cleanAiJsonResponse(String raw) {
        if (raw == null) return "[]";
        // Remove markdown code blocks if present
        return raw.replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private String extractTextFromGeminiResponse(String responseBody) {
        // Deprecated
        return "[]";
    }
}
