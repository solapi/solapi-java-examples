package net.nurigo.kotlinspringdemo

import net.nurigo.sdk.NurigoApp.initialize
import net.nurigo.sdk.message.model.Balance
import net.nurigo.sdk.message.model.Message
import net.nurigo.sdk.message.model.StorageType
import net.nurigo.sdk.message.request.MessageListRequest
import net.nurigo.sdk.message.request.MultipleMessageSendingRequest
import net.nurigo.sdk.message.request.SingleMessageSendingRequest
import net.nurigo.sdk.message.response.MessageListResponse
import net.nurigo.sdk.message.response.MultipleMessageSentResponse
import net.nurigo.sdk.message.response.SingleMessageSentResponse
import net.nurigo.sdk.message.service.DefaultMessageService
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException


@RestController
class ExampleController {
    /**
     * 발급받은 API KEY와 API Secret Key를 사용해주세요.
     */
    val messageService: DefaultMessageService =
        initialize("INSERT API KEY", "INSERT API SECRET KEY", "https://api.solapi.com")

    /**
     * 메시지 조회 예제
     */
    @GetMapping("/get-message-list")
    fun getMessageList(): MessageListResponse? {
        val response = messageService.getMessageList(MessageListRequest())
        println(response)
        return response
    }

    /**
     * 단일 메시지 발송 예제
     */
    @PostMapping("/send-one")
    fun sendOne(): SingleMessageSentResponse? {
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        /*val message = Message(
            from = "발신번호 입력",
            to = "수신번호 입력",
            text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다."
        )*/
        val message = Message(
            from = "029302266",
            to = "01051693100",
            text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다."
        )
        val response = messageService.sendOne(SingleMessageSendingRequest(message))
        println(response)
        return response
    }

    /**
     * MMS 발송 예제
     * 단일 발송, 여러 건 발송 상관없이 이용 가능
     */
    @PostMapping("/send-mms")
    @Throws(IOException::class)
    fun sendMmsByResourcePath(): SingleMessageSentResponse? {
        val resource = ClassPathResource("static/sample.jpg")
        val file = resource.file
        val imageId = messageService.uploadFile(file, StorageType.MMS, null)

        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        val message = Message(
            from = "발신번호 입력",
            to = "수신번호 입력",
            text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.",
            imageId = imageId
        )

        // 여러 건 메시지 발송일 경우 send many 예제와 동일하게 구성하여 발송할 수 있습니다.
        val response = messageService.sendOne(SingleMessageSendingRequest(message))
        println(response)
        return response
    }

    /**
     * 여러 메시지 발송 예제
     * 한 번 실행으로 최대 10,000건 까지의 메시지가 발송 가능합니다.
     */
    @PostMapping("/send-many")
    fun sendMany(): MultipleMessageSentResponse? {
        val messageList = ArrayList<Message>()
        for (i in 0..2) {
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            val message = Message(
                from = "발신번호 입력",
                to = "수신번호 입력",
                text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.$i"
            )
            messageList.add(message)
        }
        val request = MultipleMessageSendingRequest(messageList)
        // allowDuplicates를 true로 설정하실 경우 중복으로 수신번호를 입력해도 각각 발송됩니다.
        // request.setAllowDuplicates(true);
        val response = messageService.sendMany(request)
        println(response)
        return response
    }

    /**
     * 잔액 조회 예제
     */
    @GetMapping("/get-balance")
    fun getBalance(): Balance {
        val balance = messageService.getBalance()
        println(balance)
        return balance
    }
}