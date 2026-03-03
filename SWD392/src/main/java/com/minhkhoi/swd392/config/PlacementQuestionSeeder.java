package com.minhkhoi.swd392.config;

import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.entity.PlacementQuestion;
import com.minhkhoi.swd392.repository.PlacementQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Seed dữ liệu câu hỏi kiểm tra trình độ tiếng Nhật.
 * Chạy MỘT LẦN khi khởi động server, bỏ qua nếu DB đã có dữ liệu.
 * Câu hỏi theo chuẩn giáo trình Dekiru Nihongo, phân bổ đều N5→N2.
 */
@Slf4j
@Component
@Order(2) // Chạy sau DataInitializer
@RequiredArgsConstructor
public class PlacementQuestionSeeder implements CommandLineRunner {

    private final PlacementQuestionRepository repository;

    /** Số câu hỏi mục tiêu. Nếu DB có ít hơn con số này → xóa và seed lại. */
    private static final int TARGET_COUNT = 40;

    @Override
    public void run(String... args) {
        long currentCount = repository.count();
        if (currentCount >= TARGET_COUNT) {
            log.info("[PlacementSeeder] DB đã có {} câu hỏi (>= {}), bỏ qua seeding.", currentCount, TARGET_COUNT);
            return;
        }

        if (currentCount > 0) {
            log.info("[PlacementSeeder] DB có {} câu hỏi (< {}), xóa và seed lại...", currentCount, TARGET_COUNT);
            repository.deleteAll();
        }

        log.info("[PlacementSeeder] Bắt đầu seed {} câu hỏi kiểm tra trình độ...", TARGET_COUNT);
        repository.saveAll(buildQuestions());
        log.info("[PlacementSeeder] Đã seed {} câu hỏi thành công.", repository.count());
    }

    private List<PlacementQuestion> buildQuestions() {
        return List.of(

                // ── N5 – Sơ cấp ──────────────────────────────────────────────────────────

                PlacementQuestion.builder()
                        .content("「おはようございます」はいつ使いますか？")
                        .options(Map.of("A", "朝", "B", "昼", "C", "夜", "D", "いつでも"))
                        .correctAnswer("A")
                        .explanation("「おはようございます」は朝の挨拶です。")
                        .topic("Hội thoại cơ bản")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 1")
                        .build(),

                PlacementQuestion.builder()
                        .content("「___さんはどちらですか？」の「どちら」の意味は何ですか？")
                        .options(Map.of("A", "どこ（Nơi nào）", "B", "どれ（Cái nào）", "C", "だれ（Ai）", "D", "いつ（Khi nào）"))
                        .correctAnswer("A")
                        .explanation("「どちら」は「どこ」の丁寧な言い方で、場所を聞くときに使います。")
                        .topic("Từ để hỏi")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 2")
                        .build(),

                PlacementQuestion.builder()
                        .content("「これ・それ・あれ」の使い分けで、話し手と聞き手の「両方から遠い」ものを指すのはどれですか？")
                        .options(Map.of("A", "これ", "B", "それ", "C", "あれ", "D", "どれ"))
                        .correctAnswer("C")
                        .explanation("「あれ」は話し手からも聞き手からも遠いものを指します。")
                        .topic("Chỉ thị từ")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 3")
                        .build(),

                PlacementQuestion.builder()
                        .content("次の文の（　）に入る正しい助詞はどれですか？「私（　）学生です。」")
                        .options(Map.of("A", "が", "B", "は", "C", "を", "D", "に"))
                        .correctAnswer("B")
                        .explanation("「私は学生です」のように、主題を示すには「は」を使います。")
                        .topic("Trợ từ")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 2")
                        .build(),

                PlacementQuestion.builder()
                        .content("「山」の正しい読み方はどれですか？")
                        .options(Map.of("A", "かわ（kawa）", "B", "うみ（umi）", "C", "やま（yama）", "D", "そら（sora）"))
                        .correctAnswer("C")
                        .explanation("「山」は「やま（yama）」と読みます。意味는 山（núi）。")
                        .topic("Hán tự")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 5")
                        .build(),

                PlacementQuestion.builder()
                        .content("「食べます」の辞書形（từ điển thể）はどれですか？")
                        .options(Map.of("A", "食べる", "B", "食べた", "C", "食べて", "D", "食べない"))
                        .correctAnswer("A")
                        .explanation("「食べます」の辞書形は「食べる（taberu）」です。")
                        .topic("Động từ")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 6")
                        .build(),

                // ── N4 – Sơ cấp nâng cao ─────────────────────────────────────────────────

                PlacementQuestion.builder()
                        .content("「___ために」と「___ように」の違いについて、意志動詞（ý chí động từ）を使うのはどちらですか？")
                        .options(Map.of("A", "ために", "B", "ように", "C", "どちらでもいい", "D", "どちらも使えない"))
                        .correctAnswer("A")
                        .explanation("「ために」は意志動詞と共に使います。「ように」は状態動詞や変化を表します。")
                        .topic("Ngữ pháp - ために vs ように")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 12")
                        .build(),

                PlacementQuestion.builder()
                        .content("「電話をかける」の意味はどれですか？")
                        .options(Map.of("A", "Nghe điện thoại", "B", "Gọi điện thoại", "C", "Tắt điện thoại", "D", "Mua điện thoại"))
                        .correctAnswer("B")
                        .explanation("「電話をかける」は電話をかける行為、つまり「Gọi điện thoại（gọi điện）」です。")
                        .topic("Từ vựng")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 8")
                        .build(),

                PlacementQuestion.builder()
                        .content("「（動詞て形）いただけませんか」はどのような意味ですか？")
                        .options(Map.of("A", "Biểu thị mong muốn", "B", "Nhờ ai đó làm gì một cách lịch sự", "C", "Hỏi khả năng", "D", "Cảm ơn người khác"))
                        .correctAnswer("B")
                        .explanation("「〜ていただけませんか」は丁寧な依頼（nhờ vả lịch sự）の表現です。")
                        .topic("Kính ngữ")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 14")
                        .build(),

                PlacementQuestion.builder()
                        .content("「彼は来るかもしれない」のように不確かな推量を表す表現はどれですか？")
                        .options(Map.of("A", "〜でしょう", "B", "〜かもしれない", "C", "〜はずだ", "D", "〜にちがいない"))
                        .correctAnswer("B")
                        .explanation("「かもしれない」は50%以下の可能性を表す不確かな推量です。")
                        .topic("Ngữ pháp - Suy đoán")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 16")
                        .build(),

                PlacementQuestion.builder()
                        .content("次の文の（　）に入る助詞はどれですか？「駅（　）歩いて5分です。」")
                        .options(Map.of("A", "で", "B", "に", "C", "から", "D", "まで"))
                        .correctAnswer("C")
                        .explanation("「〜から〜まで」で起点（điểm xuất phát）を表します。「駅から」は駅を起点として。")
                        .topic("Trợ từ")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 7")
                        .build(),

                PlacementQuestion.builder()
                        .content("「先生に作文を直していただきました」の意味はどれですか？")
                        .options(Map.of("A", "Tôi sửa bài cho giáo viên", "B", "Giáo viên nhờ tôi sửa bài", "C", "Tôi nhờ giáo viên sửa bài cho mình", "D", "Giáo viên tự sửa bài của mình"))
                        .correctAnswer("C")
                        .explanation("「〜ていただく」は「〜てもらう」の謙譲語で、話し手が恩恵を受けることを表します。")
                        .topic("Kính ngữ")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 15")
                        .build(),

                // ── N3 – Trung cấp ────────────────────────────────────────────────────────

                PlacementQuestion.builder()
                        .content("「彼が来るにもかかわらず、パーティーは中止になった」の「にもかかわらず」の意味はどれですか？")
                        .options(Map.of("A", "Vì vậy / Chính vì thế", "B", "Mặc dù / Dù cho", "C", "Trước khi", "D", "Sau khi"))
                        .correctAnswer("B")
                        .explanation("「にもかかわらず」は逆接（đảo ngược）を表し、「〜にもかかわらず〜」で「mặc dù〜nhưng〜」。")
                        .topic("Ngữ pháp - Liên kết câu")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 3")
                        .build(),

                PlacementQuestion.builder()
                        .content("「決して〜ない」の意味はどれですか？")
                        .options(Map.of("A", "Thỉnh thoảng không", "B", "Không bao giờ / Tuyệt đối không", "C", "Hầu như không", "D", "Không cần thiết"))
                        .correctAnswer("B")
                        .explanation("「決して〜ない」は強い否定（phủ định mạnh）を表し、「絶対に〜ない」と同じ意味です。")
                        .topic("Phó từ")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 5")
                        .build(),

                PlacementQuestion.builder()
                        .content("「勉強しておく」の「〜ておく」の意味はどれですか？")
                        .options(Map.of("A", "Học xong rồi", "B", "Học để dự phòng cho sau này", "C", "Bắt đầu học", "D", "Tiếp tục học"))
                        .correctAnswer("B")
                        .explanation("「〜ておく」は「準備・将来のために〜する（làm sẵn để chuẩn bị）」の意味です。")
                        .topic("Ngữ pháp - Dạng て")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 2")
                        .build(),

                PlacementQuestion.builder()
                        .content("「友達に会うたびに、懐かしくなる」の「〜たびに」の意味はどれですか？")
                        .options(Map.of("A", "Sau khi gặp bạn", "B", "Mỗi khi gặp bạn", "C", "Vì gặp bạn", "D", "Để gặp bạn"))
                        .correctAnswer("B")
                        .explanation("「〜たびに」は「〜するたびにいつも（mỗi khi〜thì）」という習慣・反復を表します。")
                        .topic("Ngữ pháp - Tần suất")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 7")
                        .build(),

                PlacementQuestion.builder()
                        .content("「鉛筆」の正しい読み方はどれですか？")
                        .options(Map.of("A", "えんぴつ", "B", "えんてつ", "C", "えんひつ", "D", "えんぶつ"))
                        .correctAnswer("A")
                        .explanation("「鉛筆」は「えんぴつ（enpitsu）」と読みます。意味：bút chì。")
                        .topic("Hán tự")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Kanji List")
                        .build(),

                PlacementQuestion.builder()
                        .content("「彼女はピアノが弾けるし、歌も上手だ」の「〜し、〜し」の機能はどれですか？")
                        .options(Map.of("A", "Liệt kê nguyên nhân / Thêm thông tin", "B", "Điều kiện", "C", "Đối lập", "D", "Kết quả"))
                        .correctAnswer("A")
                        .explanation("「〜し、〜し」は理由や情報を並列に挙げる（liệt kê thông tin/lý do並べる）表現です。")
                        .topic("Ngữ pháp - Liệt kê")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 4")
                        .build(),

                // ── N2 – Cao cấp ──────────────────────────────────────────────────────────

                PlacementQuestion.builder()
                        .content("「彼の話は信憑性に欠ける」の「信憑性」の意味はどれですか？")
                        .options(Map.of("A", "Sự hài hước", "B", "Độ tin cậy / Tính đáng tin", "C", "Sự phức tạp", "D", "Tính độc đáo"))
                        .correctAnswer("B")
                        .explanation("「信憑性（しんぴょうせい）」は「信頼できる度合い（mức độ đáng tin cậy）」を意味します。")
                        .topic("Từ vựng nâng cao")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 2")
                        .build(),

                PlacementQuestion.builder()
                        .content("「この問題に関してはまだ議論の余地がある」の「余地」の意味はどれですか？")
                        .options(Map.of("A", "Câu trả lời rõ ràng", "B", "Không gian / Chỗ còn lại để...", "C", "Vấn đề đã được giải quyết", "D", "Rủi ro"))
                        .correctAnswer("B")
                        .explanation("「余地（よち）」は「まだ〜する可能性・空間がある（còn chỗ, còn khả năng）」を意味します。")
                        .topic("Từ vựng nâng cao")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 5")
                        .build(),

                PlacementQuestion.builder()
                        .content("「にしては」を使った文として正しいのはどれですか？")
                        .options(Map.of("A", "日本語を3ヶ月勉強したにしては、とても上手だ。", "B", "雨が降るにしては、傘を持って行こう。", "C", "勉強するにしては、図書館に行く。", "D", "毎日練習するにしては、うまくなった。"))
                        .correctAnswer("A")
                        .explanation("「にしては」は「〜という条件・背景から期待される結果とは違う（so với kỳ vọng dựa trên điều kiện đó）」を表します。")
                        .topic("Ngữ pháp N2 - にしては")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 8")
                        .build(),

                PlacementQuestion.builder()
                        .content("「〜をもって」の使い方として正しいのはどれですか？")
                        .options(Map.of("A", "本日をもって、退職いたします。", "B", "雨をもって、出かけます。", "C", "友達をもって、行きます。", "D", "勉強をもって、合格します。"))
                        .correctAnswer("A")
                        .explanation("「〜をもって」は「〜を契機として・〜の時点で（lấy〜làm mốc）」を表す硬い表現です。退職・終了などに使います。")
                        .topic("Ngữ pháp N2 - Văn viết trang trọng")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 11")
                        .build(),

                PlacementQuestion.builder()
                        .content("次のうち、「受動態（bị động cách）」の文はどれですか？")
                        .options(Map.of("A", "先生は学生を褒めた。", "B", "学生は先生に褒められた。", "C", "学生が先生を褒める。", "D", "先生が学生を褒めさせた。"))
                        .correctAnswer("B")
                        .explanation("「〜られる」の受動形を使った「学生は先生に褒められた。」が受動態です。")
                        .topic("Câu bị động")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 3")
                        .build(),

                PlacementQuestion.builder()
                        .content("「敷居が高い」の正しい意味はどれですか？")
                        .options(Map.of("A", "Đắt tiền / Tốn kém", "B", "Khó tiếp cận / Ngại ngùng vì lý do nào đó", "C", "Cửa nhà cao", "D", "Cuộc sống khắc nghiệt"))
                        .correctAnswer("B")
                        .explanation("「敷居が高い」は「不義理や気まずさなどで、その場所や人に会いに行きにくい（ngại, khó tiếp cận）」という慣用句です。")
                        .topic("Thành ngữ / Cố định ngữ")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 14")
                        .build(),

                PlacementQuestion.builder()
                        .content("「苦境に立たされる」の意味はどれですか？")
                        .options(Map.of("A", "Đứng trên cao", "B", "Rơi vào tình huống khó khăn / Bị dồn vào hoàn cảnh khó xử", "C", "Thành công vượt bậc", "D", "Đứng về phía người khác"))
                        .correctAnswer("B")
                        .explanation("「苦境に立たされる」は「困難な状況に置かれる（bị đặt vào tình thế khó khăn）」を意味するイディオムです。")
                        .topic("Thành ngữ / Cố định ngữ")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 16")
                        .build(),

                // ── N5 bổ sung ──────────────────────────────────────────────────────────

                PlacementQuestion.builder()
                        .content("数字「7」の読み方として正しいのはどれですか？")
                        .options(Map.of("A", "ろく", "B", "しち", "C", "はち", "D", "く"))
                        .correctAnswer("B")
                        .explanation("7は「しち（shichi）」または「なな（nana）」と読みます。")
                        .topic("Số đếm")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 1")
                        .build(),

                PlacementQuestion.builder()
                        .content("「今日は何曜日ですか？」の「何曜日」はどんな意味ですか？")
                        .options(Map.of("A", "Hôm nay là mấy giờ?", "B", "Hôm nay là thứ mấy?", "C", "Hôm nay là ngày mấy?", "D", "Tháng mấy?"))
                        .correctAnswer("B")
                        .explanation("「何曜日（なんようび）」は「thứ mấy」を意味します。曜日＝ngày trong tuần。")
                        .topic("Ngày tháng")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 4")
                        .build(),

                PlacementQuestion.builder()
                        .content("「本（ほん）」の助数詞として正しいのはどれですか？（鉛筆、ペン、ボトルなど細長いもの）")
                        .options(Map.of("A", "枚（まい）", "B", "個（こ）", "C", "本（ほん）", "D", "匹（ひき）"))
                        .correctAnswer("C")
                        .explanation("細長いもの（bút, chai, ...）を数えるときは助数詞「本（ほん）」を使います。")
                        .topic("Trợ số từ")
                        .jlptLevel(JlptLevel.N5)
                        .source("Dekiru Nihongo - Unit 5")
                        .build(),

                // ── N4 bổ sung ───────────────────────────────────────────────────────────

                PlacementQuestion.builder()
                        .content("「〜ながら」を使った正しい文はどれですか？")
                        .options(Map.of("A", "音楽を聴きながら、勉強します。", "B", "学校に行きながら、家にいます。", "C", "寝ながら、走ります。", "D", "食べながら、空腹です。"))
                        .correctAnswer("A")
                        .explanation("「〜ながら」は2つの動作を同時に行う（làm đồng thời 2 việc）ことを表します。矛盾する動作には使えません。")
                        .topic("Ngữ pháp - ながら")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 10")
                        .build(),

                PlacementQuestion.builder()
                        .content("「試験に合格するかどうか、心配です」の「かどうか」の意味はどれですか？")
                        .options(Map.of("A", "Vì / Do", "B", "Liệu có ... hay không", "C", "Dù sao đi nữa", "D", "Khi nào"))
                        .correctAnswer("B")
                        .explanation("「〜かどうか」は「liệu〜hay không（có hay không）」を表す間接疑問の表現です。")
                        .topic("Ngữ pháp - Câu hỏi gián tiếp")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 13")
                        .build(),

                PlacementQuestion.builder()
                        .content("「謙譲語（khiêm nhường ngữ）」で「行く・来る」を表す語はどれですか？")
                        .options(Map.of("A", "いらっしゃる", "B", "まいる", "C", "おいでになる", "D", "おこしになる"))
                        .correctAnswer("B")
                        .explanation("「まいる」は「行く・来る」の謙譲語です。「いらっしゃる・おいでになる・おこしになる」は尊敬語です。")
                        .topic("Kính ngữ - Khiêm nhường ngữ")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 15")
                        .build(),

                PlacementQuestion.builder()
                        .content("「財布を家に忘れてしまいました」の「〜てしまう」の意味はどれですか？")
                        .options(Map.of("A", "Cố ý làm gì đó", "B", "Hành động hoàn thành/không mong muốn, hối tiếc", "C", "Bắt đầu làm", "D", "Tiếp tục làm"))
                        .correctAnswer("B")
                        .explanation("「〜てしまう」は「完了（hoàn thành）や後悔・不本意（hối tiếc, ngoài ý muốn）」を表します。")
                        .topic("Ngữ pháp - てしまう")
                        .jlptLevel(JlptLevel.N4)
                        .source("Dekiru Nihongo - Unit 11")
                        .build(),

                // ── N3 bổ sung ───────────────────────────────────────────────────────────

                PlacementQuestion.builder()
                        .content("「〜わけにはいかない」の意味として正しいのはどれですか？")
                        .options(Map.of("A", "Không thể không làm", "B", "Không thể làm được (vì lý do đạo đức/xã hội)", "C", "Không muốn làm", "D", "Không cần làm"))
                        .correctAnswer("B")
                        .explanation("「〜わけにはいかない」は「道徳的・社会的理由でできない（không thể làm vì lý do đạo đức）」を表します。")
                        .topic("Ngữ pháp N3 - わけにはいかない")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 9")
                        .build(),

                PlacementQuestion.builder()
                        .content("「せっかく日本まで来たのだから、富士山に登りたい」の「せっかく」の意味はどれですか？")
                        .options(Map.of("A", "Thỉnh thoảng", "B", "Thật may mắn", "C", "Đặc biệt, cất công, dày công", "D", "Gần đây"))
                        .correctAnswer("C")
                        .explanation("「せっかく」は「わざわざ・苦労して（cất công, dành dụm công sức để）」という意味で、その機会を大切にするニュアンスがあります。")
                        .topic("Phó từ - せっかく")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 6")
                        .build(),

                PlacementQuestion.builder()
                        .content("「彼は医者というより、研究者だ」の「というより」の意味はどれですか？")
                        .options(Map.of("A", "Vì là / Bởi vì", "B", "Hơn là / Đúng hơn là", "C", "Mặc dù là", "D", "Cũng là"))
                        .correctAnswer("B")
                        .explanation("「〜というより〜だ」は「〜よりも〜の方が正確だ（đúng hơn là, chính xác hơn là）」を表します。")
                        .topic("Ngữ pháp N3 - というより")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 8")
                        .build(),

                PlacementQuestion.builder()
                        .content("「〜に反して」の意味として正しいのはどれですか？")
                        .options(Map.of("A", "Phù hợp với / Theo", "B", "Trái với / Ngược với", "C", "Nhờ vào", "D", "Cùng với"))
                        .correctAnswer("B")
                        .explanation("「〜に反して（にはんして）」は「〜に逆らって（trái với, ngược với）」を意味します。例：期待に反して＝trái với kỳ vọng。")
                        .topic("Ngữ pháp N3 - に反して")
                        .jlptLevel(JlptLevel.N3)
                        .source("Dekiru Nihongo N3 - Unit 10")
                        .build(),

                // ── N2 bổ sung ───────────────────────────────────────────────────────────

                PlacementQuestion.builder()
                        .content("「〜に先立ち」の正しい使い方はどれですか？")
                        .options(Map.of("A", "開会に先立ち、会長がスピーチをした。", "B", "雨に先立ち、傘を持つ。", "C", "勉強に先立ち、遊んだ。", "D", "試合に先立ち、負けた。"))
                        .correctAnswer("A")
                        .explanation("「〜に先立ち」は「〜に先だって（trước khi, trước khi tiến hành）」という意味で、式典や重要な行事の前に使います。")
                        .topic("Ngữ pháp N2 - に先立ち")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 9")
                        .build(),

                PlacementQuestion.builder()
                        .content("「彼女が怒るのも無理はない」の「無理はない」の意味はどれですか？")
                        .options(Map.of("A", "Không thể tức giận được", "B", "Không có lý do gì để tức giận", "C", "Không trách được, điều đó là tự nhiên", "D", "Không cần phải tức giận"))
                        .correctAnswer("C")
                        .explanation("「〜のも無理はない（むりはない）」は「〜のは当然だ（điều đó là tự nhiên, không trách được）」を意味します。")
                        .topic("Ngữ pháp N2 - 無理はない")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 12")
                        .build(),

                PlacementQuestion.builder()
                        .content("「懸念（けねん）する」の意味はどれですか？")
                        .options(Map.of("A", "Kỳ vọng / Trông đợi", "B", "Lo ngại / Quan ngại", "C", "Chuẩn bị kỹ lưỡng", "D", "Hài lòng / Thỏa mãn"))
                        .correctAnswer("B")
                        .explanation("「懸念（けねん）する」は「心配する・気にかける（lo ngại, quan ngại về điều gì đó）」を意味する硬い表現です。")
                        .topic("Từ vựng N2 - Hán Việt")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 7")
                        .build(),

                PlacementQuestion.builder()
                        .content("「紆余曲折（うよきょくせつ）を経て、ようやく完成した」の「紆余曲折」の意味はどれですか？")
                        .options(Map.of("A", "Nhanh chóng và hiệu quả", "B", "Qua nhiều gian nan, thăng trầm quanh co", "C", "Theo kế hoạch ban đầu", "D", "Liên tục thất bại"))
                        .correctAnswer("B")
                        .explanation("「紆余曲折（うよきょくせつ）」は「色々な困難や曲折（nhiều khó khăn, quanh co）を経ること」を意味する四字熟語です。")
                        .topic("Tứ tự thành ngữ - Yojijukugo")
                        .jlptLevel(JlptLevel.N2)
                        .source("Dekiru Nihongo N2 - Unit 15")
                        .build()
        );
    }
}
