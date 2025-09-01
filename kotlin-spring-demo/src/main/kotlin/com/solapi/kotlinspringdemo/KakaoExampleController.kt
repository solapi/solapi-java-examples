package com.solapi.kotlinspringdemo

import com.solapi.sdk.SolapiClient
import com.solapi.sdk.message.dto.request.SendRequestConfig
import com.solapi.sdk.message.dto.request.kakao.KakaoAlimtalkTemplateListRequest
import com.solapi.sdk.message.dto.request.kakao.KakaoAlimtalkTemplateMutationRequest
import com.solapi.sdk.message.dto.request.kakao.KakaoBrandMessageTemplateListRequest
import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse
import com.solapi.sdk.message.dto.response.kakao.KakaoAlimtalkTemplateListResponse
import com.solapi.sdk.message.dto.response.kakao.KakaoAlimtalkTemplateResponse
import com.solapi.sdk.message.dto.response.kakao.KakaoBrandMessageTemplateListResponse
import com.solapi.sdk.message.exception.SolapiEmptyResponseException
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException
import com.solapi.sdk.message.exception.SolapiUnknownException
import com.solapi.sdk.message.model.Message
import com.solapi.sdk.message.model.StorageType
import com.solapi.sdk.message.model.kakao.*
import com.solapi.sdk.message.service.DefaultMessageService
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * 모든 발송 API에는 발신, 수신번호 입력 항목에 +82 또는 +8210, 010-0000-0000 같은 형태로 기입할 수 없습니다.
 * 수/발신 가능한 예시) 01000000000, 020000000 등
 */
@RestController
@RequestMapping("/kakao")
class KakaoExampleController {
    /**
     * 발급받은 API KEY와 API Secret Key를 사용해주세요.
     */
    val messageService: DefaultMessageService =
        SolapiClient.createInstance("INSERT_API_KEY", "INSERT_API_SECRET_KEY")

    /**
     * 알림톡 한건 발송 예제
     */
    @PostMapping("/send-one-ata")
    @Throws(SolapiEmptyResponseException::class, SolapiUnknownException::class, SolapiMessageNotReceivedException::class)
    fun sendOneAta(): MultipleDetailMessageSentResponse {
        // 등록하신 카카오 비즈니스 채널의 pfId, 카카오 알림톡 템플릿의 templateId를 입력해주세요.
        val kakaoOption = KakaoOption(pfId = "", templateId = "")
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.disableSms = true

        // 알림톡 템플릿 내에 #{변수} 형태가 존재할 경우 variables를 설정해주세요.
        /*
        val variables = HashMap<String, String>()
        variables["#{변수명1}"] = "테스트"
        variables["#{변수명2}"] = "치환문구 테스트2"
        kakaoOption.variables = variables
        */

        val message = Message()
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.from = "발신번호 입력"
        message.to = "수신번호 입력"
        message.kakaoOptions = kakaoOption

        val response = messageService.send(message)
        println(response)

        return response
    }

    /**
     * 여러 메시지 발송 예제
     * 한 번 실행으로 최대 10,000건 까지의 메시지가 발송 가능합니다.
     */
    @PostMapping("/send-many-ata")
    fun sendMany(): MultipleDetailMessageSentResponse? {
        val messageList = ArrayList<Message>()
        for (i in 0..2) {
            val kakaoOption = KakaoOption()
            // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
            kakaoOption.pfId = ""
            // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
            kakaoOption.templateId = ""

            val message = Message()
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.from = "발신번호 입력"
            message.to = "수신번호 입력"
            message.text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.$i"
            message.kakaoOptions = kakaoOption

            // 기본적으로 실패 시 같은 내용으로 문자 전송을 요청하나,
            // 같은 내용이 아닌 다른 내용으로 대체발송을 원한다면 replacements값을 설정해줍니다.
            /*
            val replacementMessage = Message()
            replacementMessage.from = "발신번호 입력"
            replacementMessage.to = "수신번호 입력"
            replacementMessage.text = "실패 시 대체 발송 될 메시지입니다."
            val replacmentMessages = ArrayList<Message>()
            replacmentMessages.add(replacementMessage)
            message.replacements = replacmentMessages
            */

            messageList.add(message)
        }
        try {
            // send 메소드로 단일 Message 객체를 넣어도 동작합니다!
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
    @PostMapping("/send-ata-scheduled-messages")
    fun sendScheduledMessages(): MultipleDetailMessageSentResponse? {
        val messageList = ArrayList<Message>()
        for (i in 0..2) {
            val kakaoOption = KakaoOption()
            // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
            kakaoOption.pfId = ""
            // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
            kakaoOption.templateId = ""

            val message = Message()
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.from = "발신번호 입력"
            message.to = "수신번호 입력"
            message.text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.$i"
            message.kakaoOptions = kakaoOption

            messageList.add(message)
        }
        try {
            // 과거 시간으로 예약 발송을 진행할 경우 즉시 발송처리 됩니다.
            val localDateTime: LocalDateTime =
                LocalDateTime.parse("2022-05-27 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val zoneOffset: ZoneOffset = ZoneId.systemDefault().rules.getOffset(localDateTime)
            val instant: Instant = localDateTime.toInstant(zoneOffset)

            val config = SendRequestConfig()
            config.scheduledDate = instant

            // 단일 발송도 지원하여 ArrayList<Message> 객체가 아닌 Message 단일 객체만 넣어도 동작합니다!
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
     * 친구톡 한건 발송 예제, send many 호환
     * 친구톡 내 버튼은 최대 5개까지만 생성 가능합니다.
     */
    @PostMapping("/send-cta")
    @Throws(SolapiEmptyResponseException::class, SolapiUnknownException::class, SolapiMessageNotReceivedException::class)
    fun sendOneCta(): MultipleDetailMessageSentResponse {
        val kakaoOption = KakaoOption()
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.disableSms = true

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        kakaoOption.pfId = ""

        // 친구톡에 버튼을 넣으실 경우에만 추가해주세요.
        val kakaoButtons = ArrayList<KakaoButton>()
        // 웹링크 버튼
        val kakaoWebLinkButton = KakaoButton(
            "테스트 버튼1", KakaoButtonType.WL,
            "https://example.com", "https://example.com",
            null, null
        )

        // 앱링크 버튼
        val kakaoAppLinkButton = KakaoButton(
            "테스트 버튼2", KakaoButtonType.AL,
            null, null,
            "exampleapp://test", "exampleapp://test"
        )

        // 봇 키워드 버튼, 버튼을 클릭하면 버튼 이름으로 수신자가 발신자에게 채팅을 보냅니다.
        val kakaoBotKeywordButton = KakaoButton(
            "테스트 버튼3", KakaoButtonType.BK, null, null, null, null
        )

        // 메시지 전달 버튼, 버튼을 클릭하면 버튼 이름과 친구톡 메시지 내용을 포함하여 수신자가 발신자에게 채팅을 보냅니다.
        val kakaoMessageDeliveringButton = KakaoButton(
            "테스트 버튼4", KakaoButtonType.MD, null, null, null, null
        )

        /*
         * 상담톡 전환 버튼, 상담톡 서비스를 이용하고 있을 경우 상담톡으로 전환. 상담톡 서비스 미이용시 해당 버튼 추가될 경우 발송 오류 처리됨.
         * @see <a href="https://business.kakao.com/info/bizmessage/">상담톡 딜러사 확인</a>
         */
        /*KakaoButton kakaoBotCustomerButton = new KakaoButton(
                "테스트 버튼6", KakaoButtonType.BC, null, null, null, null
        );*/

        // 봇전환 버튼, 해당 비즈니스 채널에 카카오 챗봇이 없는 경우 동작안함.
        // KakaoButton kakaoBotTransferButton = new KakaoButton("테스트 버튼7", KakaoButtonType.BT, null, null, null, null);
        kakaoButtons.add(kakaoWebLinkButton)
        kakaoButtons.add(kakaoAppLinkButton)
        kakaoButtons.add(kakaoBotKeywordButton)
        kakaoButtons.add(kakaoMessageDeliveringButton)
        kakaoOption.buttons = kakaoButtons

        val message = Message()
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.from = "발신번호 입력"
        message.to = "수신번호 입력"
        message.text = "친구톡 테스트 메시지"
        message.kakaoOptions = kakaoOption

        val response = messageService.send(message)
        println(response)
        return response
    }

    /**
     * 친구톡 이미지 단건 발송, send many 호환
     * 친구톡 내 버튼은 최대 5개까지만 생성 가능합니다.
     */
    @PostMapping("/send-cti")
    @Throws(IOException::class, SolapiEmptyResponseException::class, SolapiUnknownException::class, SolapiMessageNotReceivedException::class)
    fun sendOneCti(): MultipleDetailMessageSentResponse {
        val resource = ClassPathResource("static/cti.jpg")
        val file = resource.file
        // 이미지 크기는 가로 500px 세로 250px 이상이어야 합니다, 링크도 필수로 기입해주세요.
        val imageId = messageService.uploadFile(file, StorageType.KAKAO, "https://example.com")

        val kakaoOption = KakaoOption()
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.disableSms = true

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        kakaoOption.pfId = ""
        kakaoOption.imageId = imageId

        val message = Message()
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.from = "발신번호 입력"
        message.to = "수신번호 입력"
        message.text = "테스트"
        message.kakaoOptions = kakaoOption

        val response = messageService.send(message)
        println(response)
        return response
    }

    /**
     * 등록한 카카오 브랜드 메시지 템플릿 조회 예제
     * @return KakaoBrandMessageTemplateListResponse
     */
    @GetMapping("get-kakao-bms-templates")
    fun getKakaoBrandMessageTemplates(): KakaoBrandMessageTemplateListResponse {
        /// 아래 Request 파라미터 코드를 추가하여 여러 검색 조건을 넣으실 수 있습니다!
        val request = KakaoBrandMessageTemplateListRequest()
        // request.pfId = "등록한 비즈니스 채널 pfId"
        // request.brandTemplateId = "등록한 브랜드 메시지 템플릿 ID"
        // 그 외의 검색조건은 KakaoBrandMessageTemplateListRequest 내 프로퍼티를 참조해주세요!
        return messageService.getKakaoBrandMessageTemplates(request)
    }

    /**
     * 카카오 브랜드 메시지 발송 예제, 단 건 및 여러 건 발송 모두 지원합니다.
     * 현재 targeting 타입 중 M, N의 경우는 카카오 측에서 인허가된 채널만 사용하실 수 있습니다.
     * 그 외의 모든 채널은 I 타입만 사용 가능합니다.
     * @return MultipleDetailMessageSentResponse
     */
    @PostMapping("send-brand-message")
    fun sendBrandMessage(): MultipleDetailMessageSentResponse? {
        try {
            val kakaoOption = KakaoOption()
            kakaoOption.pfId = "연동한 비즈니스 채널의 pfId"
            kakaoOption.templateId = "등록한 브랜드 메시지 템플릿의 템플릿 ID"

            // 브랜드 메시지 템플릿 내 치환문구(#{변수명}) 형식이 있다면 아래와 같은 코드를 추가해주세요!
            // val variables = mutableMapOf<String, String>()
            // variables.put("#{변수명1}", "홍길동")
            // variables.put("#{변수명2}", "김철수")
            // kakaoOption.variables = variables

            val message = Message()
            // 브랜드 메시지는 현재 대체 발송이 지원되지 않습니다!
            message.to = "수신번호 입력"
            message.kakaoOptions = kakaoOption

            //  만약 여러 건을 발송하고 싶으시다면 아래와 같은 코드로 변경하여 발송 해 보세요!
            // val messages = ArrayList<Message>()
            // messages.add(message)
            // 이후 최대 10,000건 까지 원하는 만큼 메시지 객체를 추가할 수 있습니다!
            // messageService.send(messages)

            /// 여러 건 발송을 진행 하시려면 주석처리 해둔 messages로 파라미터를 변경합니다.
            return messageService.send(message)
        } catch (sne: SolapiMessageNotReceivedException) {
            println("발송 접수에 실패한 메시지 목록: " + sne.failedMessageList)
            println(sne.message)
        } catch (e: SolapiEmptyResponseException) {
            println(e.message)
        } catch (e: SolapiUnknownException) {
            println(e.message)
        }
        return null
    }

    /**
     * 알림톡 템플릿 생성 예제
     * <a href="https://developers.solapi.com/references/kakao/templates/createTemplate">알림톡 템플릿 생성 개발문서</a>
     * @return KakaoAlimtalkTemplateResponse
     */
    @PostMapping("create-alimtalk-template")
    fun createKakaoAlimtalkTemplate(): KakaoAlimtalkTemplateResponse {
        // 알림톡 템플릿을 생성하려면 반드시 카테고리는 사전에 조회해야 합니다. 카테고리 코드 조회 후 실제 필요에 맞게 값을 request 객체에 넣어주시면 됩니다!
        val categories = messageService.getKakaoAlimtalkTemplateCategories()
        val category = categories[0].code

        // https://developers.solapi.com/references/kakao/templates/createTemplate 페이지를 참고하여 템플릿 제작에 필요한 파라미터를 넣어 보세요!
        // 또는 /// <a href="https://solapi.github.io/solapi-kotlin/solapi.sdk/com.solapi.sdk.message.dto.request.kakao/-kakao-alimtalk-template-mutation-request/index.html">SOLAPI Kotlin SDK 문서</a>
        // 항목을 참고 해 주세요!

        val request = KakaoAlimtalkTemplateMutationRequest()
        request.channelId = "등록한 비즈니스 채널의 pfId" // 혹은 channelGroupId 기입
        request.name = "지정할 알림톡 템플릿 제목"
        request.content = "알림톡 템플릿 내용"
        request.categoryCode = category

        return messageService.createKakaoAlimtalkTemplate(request)
    }

    /**
     * 알림톡 템플릿 목록 조회 예제
     * @return KakaoAlimtalkTemplateListResponse
     */
    @GetMapping("get-alimtalk-templates")
    fun getKakaoAlimtalkTemplates(): KakaoAlimtalkTemplateListResponse {
        val request = KakaoAlimtalkTemplateListRequest()

        // dateCreated, dateUpdated 등의 날짜 별 조회를 진행할 땐 아래와 같은 코드를 넣어주세요!
        // val dateCreatedQueryCondition = KakaoTemplateDateQuery.KakaoAlimtalkTemplateDateQueryCondition.GREATER_THEN_OR_EQUAL
        // val dateCreatedQuery = KakaoTemplateDateQuery(Instant.parse("2025-09-01T00:00:00Z"), dateCreatedQueryCondition)
        // request.dateCreated = dateCreatedQuery

        // status를 조회할 땐 KakaoAlimtalkTemplateStatus의 enum타입을 request.status에 넣어주세요!
        // request.status = KakaoAlimtalkTemplateStatus.APPROVED // 혹은 PENDING 등..

        // 검색할 건 수, 값 미지정 시 20건 조회, 최대 500건 까지 설정 가능합니다.
        // request.limit = 1

        // 조회 후 다음 페이지로 넘어가려면 이전에 조회할 당시 나왔던 nextKey 항목을 입력 해 주셔야 합니다!
        // request.startKey = "조회 한 nextKey 데이터"

        // 그 외 다른 조회 조건들은 <a href="https://developers.solapi.com/references/kakao/templates/getTemplateList">SOLAPI 개발문서 항목</a>을 참고해주세요!
        return messageService.getKakaoAlimtalkTemplates(request)
    }

    /**
     * 알림톡 템플릿 단 건 조회 예제
     * @return KakaoAlimtalkTemplateResponse
     */
    @GetMapping("get-alimtalk-template")
    fun getKakaoAlimtalkTemplate(): KakaoAlimtalkTemplateResponse {
        return messageService.getKakaoAlimtalkTemplate("조회 할 알림톡 템플릿 ID")
    }

    /**
     * 알림톡 템플릿 수정 예제
     * 알림톡 템플릿을 수정 할 때에는 channelId, channelGroupId를 넣으실 수 없습니다!
     * <a href="https://developers.solapi.com/references/kakao/templates/updateTemplate">알림톡 템플릿 수정 API 문서</a>
     * @return KakaoAlimtalkTemplateResponse
     */
    @PutMapping("update-alimtalk-template")
    fun updateKakaoAlimtalkTemplate(): KakaoAlimtalkTemplateResponse {
        val request = KakaoAlimtalkTemplateMutationRequest()
        request.content = "수정할 알림톡 템플릿 내용"

        // 그 외의 수정 조건은 <a href="https://developers.solapi.com/references/kakao/templates/updateTemplate">알림톡 템플릿 수정 API 문서</a>를 확인하시거나
        // <a href="https://solapi.github.io/solapi-kotlin/solapi.sdk/com.solapi.sdk.message.dto.request.kakao/-kakao-alimtalk-template-mutation-request/index.html">SOLAPI Kotlin SDK 문서</a>
        // 항목을 참고 해주세요!

        return messageService.updateKakaoAlimtalkTemplate("수정 할 알림톡 템플릿 ID", request)
    }

    /**
     * 알림톡 템플릿 이름 수정 예제
     * 이름을 수정 할 때에는 다른 알림톡 템플릿과 중복하여 수정하실 수 있습니다!
     * @return KakaoAlimtalkTemplateResponse
     */
    @PatchMapping("update-alimtalk-template-name")
    fun updateNameKakaoAlimtalkTemplate(): KakaoAlimtalkTemplateResponse {
        return messageService.updateKakaoAlimtalkTemplateName("이름을 수정 할 알림톡 템플릿 ID", "수정 할 이름")
    }

    /**
     * 알림톡 템플릿 검수요청 예제
     * @return KakaoAlimtalkTemplateResponse
     */
    @PatchMapping("inspection-request-alimtalk-template")
    fun inspectionRequestAlimtalkTemplate(): KakaoAlimtalkTemplateResponse {
        return messageService.requestKakaoAlimtalkTemplateInspection("검수 할 알림톡 템플릿 ID")
    }

    /**
     * 알림톡 템플릿 삭제 예제
     * @return KakaoAlimtalkTemplateResponse
     */
    @DeleteMapping("remove-alimtalk-template")
    fun removeAlimtalkTemplate(): KakaoAlimtalkTemplateResponse {
        return messageService.removeKakaoAlimtalkTemplate("삭제 할 알림톡 템플릿 ID")
    }
}