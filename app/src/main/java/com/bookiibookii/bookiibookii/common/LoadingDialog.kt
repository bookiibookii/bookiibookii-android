import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.R

class LoadingDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_com_loading) // 작성하신 xml 파일 이름

        // 취소 불가능하게 설정 (뒤로가기, 바깥 터치 막기)
        setCancelable(false)

        // 중요: 다이얼로그를 전체 화면으로 만들기 위한 설정
        window?.apply {
            // 1. 다이얼로그 기본 배경(흰색 사각형)을 투명하게 만듦 -> 그래야 XML의 배경색이 나옴
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 2. 너비와 높이를 화면 가득 채우기 (MATCH_PARENT)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
}