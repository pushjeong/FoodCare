import com.AzaAza.foodcare.models.SignUpRequest
import com.AzaAza.foodcare.models.LoginRequest
import com.AzaAza.foodcare.models.SignupResponse
import com.AzaAza.foodcare.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApiService {
    // 회원가입
    @POST("/user")
    fun signUp(@Body request: SignUpRequest): Call<SignupResponse>

    // 로그인 (login_id + password)
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<UserResponse>


    // (Optional) 전체 사용자 조회: GET /user
    @GET("/user")
    fun getUsers(): Call<List<UserResponse>>
}