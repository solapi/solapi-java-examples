package com.solapi.kotlinspringdemo

import com.solapi.sdk.SolapiClient
import com.solapi.sdk.message.dto.request.MessageListRequest
import com.solapi.sdk.message.dto.request.SendRequestConfig
import com.solapi.sdk.message.dto.response.MessageListResponse
import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse
import com.solapi.sdk.message.exception.SolapiEmptyResponseException
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException
import com.solapi.sdk.message.exception.SolapiUnknownException
import com.solapi.sdk.message.model.Balance
import com.solapi.sdk.message.model.Message
import com.solapi.sdk.message.model.StorageType
import com.solapi.sdk.message.service.DefaultMessageService
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@RestController
class ExampleController {
    // 반드시 계정 내 등록된 유효한 API 키, API Secret Key를 입력해주셔야 합니다!
    val messageService: DefaultMessageService =
        SolapiClient.INSTANCE.createInstance("INSERT_API_KEY", "INSERT_API_SECRET_KEY")

    /**
     * 메시지 조회 예제
     */
    @GetMapping("/get-message-list")
    fun getMessageList(): MessageListResponse {
        // 검색 조건이 있는 경우에 MessagListRequest를 초기화 하여 getMessageList 함수에 파라미터로 넣어서 검색할 수 있습니다!.
        // 수신번호와 발신번호는 반드시 -,* 등의 특수문자를 제거한 01012345678 형식으로 입력해주셔야 합니다!
        val request = MessageListRequest()

        // 검색할 건 수, 값 미지정 시 20건 조회, 최대 500건 까지 설정 가능
        // request.limit = 1

        // 조회 후 다음 페이지로 넘어가려면 조회 당시 마지막의 messageId를 입력해주셔야 합니다!
        // request.startKey = "메시지 ID"

        // request.to = "검색할 수신번호"
        // request.from = "검색할 발신번호"

        // 메시지 상태 검색, PENDING은 대기 건, SENDING은 발송 중,COMPLETE는 발송완료, FAILED는 발송에 실패한 모든 건입니다.
        /*
        request.status = MessageStatusType.PENDING
        request.status = MessageStatusType.SENDING
        request.status = MessageStatusType.COMPLETE
        request.status = MessageStatusType.FAILED
        */

        // request.messageId = "검색할 메시지 ID"

        // 검색할 메시지 목록
        // val messageIds = mutableListOf<String>()
        // messageIds.add("검색할 메시지 ID");
        // request.messageIds = messageIds;

        // 조회 할 메시지 유형 검색, 유형에 대한 값은 아래 내용을 참고해주세요!
        // SMS: 단문
        // LMS: 장문
        // MMS: 사진문자
        // ATA: 알림톡
        // CTA: 친구톡
        // CTI: 이미지 친구톡
        // NSA: 네이버 스마트알림
        // RCS_SMS: RCS 단문
        // RCS_LMS: RCS 장문
        // RCS_MMS: RCS 사진문자
        // RCS_TPL: RCS 템플릿문자
        // request.type = "조회 할 메시지 유형"

        return messageService.getMessageList(request)
    }

    /**
     * 단일 메시지 발송 예제
     */
    @PostMapping("/send-one")
    fun sendOne(): MultipleDetailMessageSentResponse? {
        return try {
            val message = Message()
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.from = "발신번호 입력"
            message.to = "수신번호 입력"
            message.text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다."

            val response = messageService.send(message)
            println(response)

            response
        } catch (e: Exception) {
            e.fillInStackTrace()
            null
        }
    }

    /**
     * MMS 발송 예제
     * 단일 발송, 여러 건 발송 상관없이 이용 가능
     */
    @PostMapping("/send-mms")
    @Throws(IOException::class, SolapiMessageNotReceivedException::class, SolapiUnknownException::class, SolapiEmptyResponseException::class)
    fun sendMmsByResourcePath(): MultipleDetailMessageSentResponse {
        val resource = ClassPathResource("static/sample.jpg")
        val file = resource.file
        val imageId = messageService.uploadFile(file, StorageType.MMS)

        val message = Message()
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.from = "발신번호 입력"
        message.to = "수신번호 입력"
        message.text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다."
        message.imageId = imageId

        // 여러 건 메시지 발송일 경우 send many 예제와 동일하게 구성하여 발송할 수 있습니다.
        val response = messageService.send(message)
        println(response)

        return response
    }

    /**
     * 여러 메시지 발송 예제
     * 한 번 실행으로 최대 10,000건 까지의 메시지가 발송 가능합니다.
     */
    @PostMapping("/send-many")
    fun sendMany(): MultipleDetailMessageSentResponse? {
        val messageList = ArrayList<Message>()
        for (i in 0..2) {
            val message = Message()
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.from = "발신번호 입력"
            message.to = "수신번호 입력"
            message.text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.$i"

            // 메시지 건건 마다 사용자가 원하는 커스텀 값(특정 주문/결제 건의 ID를 넣는등)을 map 형태로 기입하여 전송 후 확인해볼 수 있습니다!
            /*val map = HashMap<String, String>()
            map["키 입력"] = "값 입력"
            message.customFields = map*/

            messageList.add(message)
        }
        try {
            // send 메소드로 단일 Message 객체를 넣어도 동작합니다!
            // 세 번째 파라미터인 showMessageList 값을 true로 설정할 경우 MultipleDetailMessageSentResponse에서 MessageList를 리턴하게 됩니다!
            val response = messageService.send(messageList)

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            // val response = this.messageService.send(messageList, true);
            println(response)
            return response
        } catch (exception: SolapiMessageNotReceivedException) {
            println(exception.failedMessageList)
            println(exception.message)
        } catch (exception: Exception) {
            println(exception.message)
        }
        return null
    }

    /**
     * 예약 발송 예제(단건 및 여러 건 발송을 지원합니다)
     */
    @PostMapping("/send-scheduled-messages")
    fun sendScheduledMessages(): MultipleDetailMessageSentResponse? {
        val messageList = ArrayList<Message>()
        for (i in 0..2) {
            val message = Message()
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.from = "발신번호 입력"
            message.to = "수신번호 입력"
            message.text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.$i"

            messageList.add(message)
        }
        try {
            // 과거 시간으로 예약 발송을 진행할 경우 즉시 발송처리 됩니다.
            val localDateTime: LocalDateTime =
                LocalDateTime.parse("2025-08-20 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val zoneOffset: ZoneOffset = ZoneId.systemDefault().rules.getOffset(localDateTime)
            val instant: Instant = localDateTime.toInstant(zoneOffset)

            // 단일 발송도 지원하여 ArrayList<Message> 객체가 아닌 Message 단일 객체만 넣어도 동작합니다!
            val config = SendRequestConfig()
            config.scheduledDate = instant
            val response: MultipleDetailMessageSentResponse = messageService.send(messageList, config)

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            // val response = this.messageService.send(messageList, instant, true);
            println(response)
            return response
        } catch (exception: SolapiMessageNotReceivedException) {
            println(exception.failedMessageList)
            println(exception.message)
        } catch (exception: Exception) {
            println(exception.message)
        }
        return null
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