// UserApiService.kt
import com.AzaAza.foodcare.models.SignUpRequest
import com.AzaAza.foodcare.models.LoginRequest
import com.AzaAza.foodcare.models.SignupResponse
import com.AzaAza.foodcare.models.UserResponse
import com.AzaAza.foodcare.models.VerificationRequestDto
import com.AzaAza.foodcare.models.VerificationConfirmDto
import com.AzaAza.foodcare.models.VerificationResponseDto
import com.AzaAza.foodcare.models.PasswordChangeRequestDto
import com.AzaAza.foodcare.models.PasswordChangeResponseDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {
    // 회원가입
    @POST("/user")
    fun signUp(@Body request: SignUpRequest): Call<SignupResponse>

    // 로그인 (login_id + password)
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<UserResponse>

    // 이메일 인증 코드 요청
    @POST("/user/verify/request")
    suspend fun requestVerificationCode(@Body request: VerificationRequestDto): VerificationResponseDto

    // 이메일 인증 코드 확인
    @POST("/user/verify/confirm")
    suspend fun confirmVerificationCode(@Body request: VerificationConfirmDto): VerificationResponseDto

    // (Optional) 전체 사용자 조회: GET /user
    @GET("/user")
    fun getUsers(): Call<List<UserResponse>>

    @GET("/user")
    fun getUserListAsSignUpRequest(): Call<List<SignUpRequest>>

    @DELETE("/user/{login_id}")
    fun deleteUser(@Path("login_id") loginId: String): Call<Void>

    // 비밀번호 변경
    @POST("user/password/change")
    suspend fun changePassword(@Body request: PasswordChangeRequestDto): PasswordChangeResponseDto
}