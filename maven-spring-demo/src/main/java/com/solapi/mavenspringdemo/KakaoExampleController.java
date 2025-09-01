package com.solapi.mavenspringdemo;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.dto.request.SendRequestConfig;
import com.solapi.sdk.message.dto.request.kakao.KakaoAlimtalkTemplateListRequest;
import com.solapi.sdk.message.dto.request.kakao.KakaoAlimtalkTemplateMutationRequest;
import com.solapi.sdk.message.dto.request.kakao.KakaoBrandMessageTemplateListRequest;
import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse;
import com.solapi.sdk.message.dto.response.kakao.KakaoAlimtalkTemplateListResponse;
import com.solapi.sdk.message.dto.response.kakao.KakaoAlimtalkTemplateResponse;
import com.solapi.sdk.message.dto.response.kakao.KakaoBrandMessageTemplateListResponse;
import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.model.StorageType;
import com.solapi.sdk.message.model.kakao.*;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 모든 발송 API에는 발신, 수신번호 입력 항목에 +82 또는 +8210 같은 형태로 기입할 수 없습니다.
 * 수/발신 가능한 예시) 01000000000, 020000000 등
 */
@RestController
@RequestMapping("/kakao")
public class KakaoExampleController {

    private final DefaultMessageService messageService;

    /**
     * 발급받은 API KEY와 API Secret Key를 사용해주세요.
     */
    public KakaoExampleController() {
        // 반드시 계정 내 등록된 유효한 API 키, API Secret Key를 입력해주셔야 합니다!
        this.messageService = SolapiClient.INSTANCE.createInstance("INSERT_API_KEY", "INSERT_API_SECRET_KEY");
    }

    /**
     * 알림톡 한건 발송 예제
     */
    @PostMapping("/send-one-ata")
    public MultipleDetailMessageSentResponse sendOneAta() throws SolapiEmptyResponseException, SolapiUnknownException, SolapiMessageNotReceivedException {
        KakaoOption kakaoOption = new KakaoOption();
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.setDisableSms(true);

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        kakaoOption.setPfId("");
        // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
        kakaoOption.setTemplateId("");

        // 알림톡 템플릿 내에 #{변수} 형태가 존재할 경우 variables를 설정해주세요.
        /*
        HashMap<String, String> variables = new HashMap<>();
        variables.put("#{변수명1}", "테스트");
        variables.put("#{변수명2}", "치환문구 테스트2");
        kakaoOption.setVariables(variables);
        */

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("발신번호 입력");
        message.setTo("수신번호 입력");
        message.setKakaoOptions(kakaoOption);

        MultipleDetailMessageSentResponse response = this.messageService.send(message);
        System.out.println(response);

        return response;
    }

    /**
     * 여러 메시지 발송 예제
     * 한 번 실행으로 최대 10,000건 까지의 메시지가 발송 가능합니다.
     */
    @PostMapping("/send-many-ata")
    public MultipleDetailMessageSentResponse sendMany() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            KakaoOption kakaoOption = new KakaoOption();
            // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
            kakaoOption.setPfId("");
            // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
            kakaoOption.setTemplateId("");

            Message message = new Message();
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.setFrom("발신번호 입력");
            message.setTo("수신번호 입력");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);
            message.setKakaoOptions(kakaoOption);

            // 기본적으로 실패 시 같은 내용으로 문자 전송을 요청하나,
            // 같은 내용이 아닌 다른 내용으로 대체발송을 원한다면 replacements값을 설정해줍니다.
            /*
            Message replacementMessage = new Message();
            replacementMessage.setFrom("발신번호 입력");
            replacementMessage.setTo("수신번호 입력");
            replacementMessage.setText("실패 시 대체 발송 될 메시지입니다.");
            ArrayList<Message> replacmentMessages = new ArrayList<>();
            replacmentMessages.add(replacementMessage);
            message.setReplacements(replacmentMessages);
            */

            messageList.add(message);
        }

        try {
            // send 메소드로 단일 Message 객체를 넣어도 동작합니다!
            MultipleDetailMessageSentResponse response = this.messageService.send(messageList);

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            //MultipleDetailMessageSentResponse response = this.messageService.send(messageList, true);

            System.out.println(response);

            return response;
        } catch (SolapiMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    @PostMapping("/send-ata-scheduled-messages")
    public MultipleDetailMessageSentResponse sendScheduledMessages() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            KakaoOption kakaoOption = new KakaoOption();
            // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
            kakaoOption.setPfId("");
            // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
            kakaoOption.setTemplateId("");

            Message message = new Message();
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.setFrom("발신번호 입력");
            message.setTo("수신번호 입력");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);
            message.setKakaoOptions(kakaoOption);

            messageList.add(message);
        }

        try {
            // 과거 시간으로 예약 발송을 진행할 경우 즉시 발송처리 됩니다.
            LocalDateTime localDateTime = LocalDateTime.parse("2022-05-27 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(localDateTime);
            Instant instant = localDateTime.toInstant(zoneOffset);

            SendRequestConfig config = new SendRequestConfig();
            config.setScheduledDate(instant);

            // 단일 발송도 지원하여 ArrayList<Message> 객체가 아닌 Message 단일 객체만 넣어도 동작합니다!
            MultipleDetailMessageSentResponse response = this.messageService.send(messageList, config);

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            //MultipleDetailMessageSentResponse response = this.messageService.send(messageList, instant, true);

            System.out.println(response);

            return response;
        } catch (SolapiMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    /**
     * 친구톡 한건 발송 예제, send 호환, 다량 발송의 경우 위 send-many-ata 코드를 참조해보세요!
     * 친구톡 내 버튼은 최대 5개까지만 생성 가능합니다.
     */
    @PostMapping("/send-cta")
    public MultipleDetailMessageSentResponse sendOneCta() throws SolapiEmptyResponseException, SolapiUnknownException, SolapiMessageNotReceivedException {
        KakaoOption kakaoOption = new KakaoOption();
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.setDisableSms(true);

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        kakaoOption.setPfId("");

        // 친구톡에 버튼을 넣으실 경우에만 추가해주세요.
        ArrayList<KakaoButton> kakaoButtons = new ArrayList<>();
        // 웹링크 버튼
        KakaoButton kakaoWebLinkButton = new KakaoButton(
                "테스트 버튼1", KakaoButtonType.WL,
                "https://example.com", "https://example.com",
                null, null
        );

        // 앱링크 버튼
        KakaoButton kakaoAppLinkButton = new KakaoButton(
                "테스트 버튼2", KakaoButtonType.AL,
                null, null,
                "exampleapp://test", "exampleapp://test"
        );

        // 봇 키워드 버튼, 버튼을 클릭하면 버튼 이름으로 수신자가 발신자에게 채팅을 보냅니다.
        KakaoButton kakaoBotKeywordButton = new KakaoButton(
                "테스트 버튼3", KakaoButtonType.BK, null, null, null, null
        );

        // 메시지 전달 버튼, 버튼을 클릭하면 버튼 이름과 친구톡 메시지 내용을 포함하여 수신자가 발신자에게 채팅을 보냅니다.
        KakaoButton kakaoMessageDeliveringButton = new KakaoButton(
                "테스트 버튼4", KakaoButtonType.MD, null, null, null, null
        );

        /*
         * 상담톡 전환 버튼, 상담톡 서비스를 이용하고 있을 경우 상담톡으로 전환. 상담톡 서비스 미이용시 해당 버튼 추가될 경우 발송 오류 처리됨.
         * @see <a href="https://business.kakao.com/info/bizmessage/">상담톡 딜러사 확인</a>
         */
        /*KakaoButton kakaoBotCustomerButton = new KakaoButton(
                "테스트 버튼6", KakaoButtonType.BC, null, null, null, null
        );*/

        // 봇전환 버튼, 해당 비즈니스 채널에 카카오 챗봇이 없는 경우 동작안함.
        // KakaoButton kakaoBotTransferButton = new KakaoButton("테스트 버튼7", KakaoButtonType.BT, null, null, null, null);

        kakaoButtons.add(kakaoWebLinkButton);
        kakaoButtons.add(kakaoAppLinkButton);
        kakaoButtons.add(kakaoBotKeywordButton);
        kakaoButtons.add(kakaoMessageDeliveringButton);

        kakaoOption.setButtons(kakaoButtons);

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("발신번호 입력");
        message.setTo("수신번호 입력");
        message.setText("친구톡 테스트 메시지");
        message.setKakaoOptions(kakaoOption);

        MultipleDetailMessageSentResponse response = this.messageService.send(message);
        System.out.println(response);

        return response;
    }

    /**
     * 친구톡 이미지 단건 발송, send 호환, 다량 발송의 경우 위 send-many-ata 코드를 참조해보세요!
     * 친구톡 내 버튼은 최대 5개까지만 생성 가능합니다.
     */
    @PostMapping("/send-cti")
    public MultipleDetailMessageSentResponse sendOneCti() throws IOException, SolapiEmptyResponseException, SolapiUnknownException, SolapiMessageNotReceivedException {
        ClassPathResource resource = new ClassPathResource("static/cti.jpg");
        File file = resource.getFile();
        // 이미지 크기는 가로 500px 세로 250px 이상이어야 합니다, 링크도 필수로 기입해주세요.
        String imageId = this.messageService.uploadFile(file, StorageType.KAKAO, "https://example.com");

        KakaoOption kakaoOption = new KakaoOption();
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.setDisableSms(true);

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        kakaoOption.setPfId("");
        kakaoOption.setImageId(imageId);

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("발신번호 입력");
        message.setTo("수신번호 입력");
        message.setText("테스트");
        message.setKakaoOptions(kakaoOption);

        MultipleDetailMessageSentResponse response = this.messageService.send(message);
        System.out.println(response);

        return response;
    }

    /**
     * 등록한 카카오 브랜드 메시지 템플릿 조회 예제
     * @return KakaoBrandMessageTemplateListResponse
     */
    @GetMapping("get-kakao-bms-templates")
    public KakaoBrandMessageTemplateListResponse getKakaoBrandMessageTemplates() {
        /// 아래 Request 파라미터 코드를 추가하여 여러 검색 조건을 넣으실 수 있습니다!
        KakaoBrandMessageTemplateListRequest request = new KakaoBrandMessageTemplateListRequest();
        // request.setPfId("등록한 비즈니스 채널 pfId");
        // request.setBrandTemplateId("등록한 브랜드 메시지 템플릿 ID");
        // 그 외의 검색조건은 KakaoBrandMessageTemplateListRequest 내 프로퍼티를 참조해주세요!
        return this.messageService.getKakaoBrandMessageTemplates(request);
    }

    /**
     * 카카오 브랜드 메시지 발송 예제, 단 건 및 여러 건 발송 모두 지원합니다.
     * 현재 targeting 타입 중 M, N의 경우는 카카오 측에서 인허가된 채널만 사용하실 수 있습니다.
     * 그 외의 모든 채널은 I 타입만 사용 가능합니다.
     * @return MultipleDetailMessageSentResponse
     */
    @PostMapping("send-brand-message")
    public MultipleDetailMessageSentResponse sendBrandMessage() {
        try {
            KakaoOption kakaoOption = new KakaoOption();
            kakaoOption.setPfId("연동한 비즈니스 채널의 pfId");
            kakaoOption.setTemplateId("등록한 브랜드 메시지 템플릿의 템플릿 ID");

            /// 브랜드 메시지 템플릿 내 치환문구(#{변수명}) 형식이 있다면 아래와 같은 코드를 추가해주세요!
            // HashMap<String, String> variables = new HashMap<>();
            // variables.put("#{변수명1}", "홍길동");
            // variables.put("#{변수명2}", "김철수");
            // kakaoOption.setVariables(variables);

            Message message = new Message();
            /// 브랜드 메시지는 현재 대체 발송이 지원되지 않습니다!
            message.setTo("수신번호 입력");
            message.setKakaoOptions(kakaoOption);

            ///  만약 여러 건을 발송하고 싶으시다면 아래와 같은 코드로 변경하여 발송 해 보세요!
            // ArrayList<Message> messages = new ArrayList<>();
            // messages.add(message);
            // 이후 최대 10,000건 까지 원하는 만큼 메시지 객체를 추가할 수 있습니다!
            // this.messageService.send(messages);

            /// 여러 건 발송을 진행 하시려면 주석처리 해둔 messages로 파라미터를 변경합니다.
            return this.messageService.send(message);
        } catch (SolapiMessageNotReceivedException sne) {
            System.out.println("발송 접수에 실패한 메시지 목록: " + sne.getFailedMessageList());
            System.out.println(sne.getMessage());
        } catch (SolapiEmptyResponseException | SolapiUnknownException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * 알림톡 템플릿 생성 예제
     * <a href="https://developers.solapi.com/references/kakao/templates/createTemplate">알림톡 템플릿 생성 개발문서</a>
     * @return KakaoAlimtalkTemplateResponse
     */
    @PostMapping("create-alimtalk-template")
    public KakaoAlimtalkTemplateResponse createKakaoAlimtalkTemplate() {
        /// 알림톡 템플릿을 생성하려면 반드시 카테고리는 사전에 조회해야 합니다. 카테고리 코드 조회 후 실제 필요에 맞게 값을 request 객체에 넣어주시면 됩니다!
        List<KakaoAlimtalkTemplateCategory> categories = this.messageService.getKakaoAlimtalkTemplateCategories();
        String category = categories.get(0).getCode();

        /// https://developers.solapi.com/references/kakao/templates/createTemplate 페이지를 참고하여 템플릿 제작에 필요한 파라미터를 넣어 보세요!
        /// 또는 /// <a href="https://solapi.github.io/solapi-kotlin/solapi.sdk/com.solapi.sdk.message.dto.request.kakao/-kakao-alimtalk-template-mutation-request/index.html">SOLAPI Kotlin SDK 문서</a>
        /// 항목을 참고 해 주세요!

        KakaoAlimtalkTemplateMutationRequest request = new KakaoAlimtalkTemplateMutationRequest();
        request.setChannelId("등록한 비즈니스 채널의 pfId"); // 혹은 channelGroupId 기입
        request.setName("지정할 알림톡 템플릿 제목");
        request.setContent("알림톡 템플릿 내용");
        request.setCategoryCode(category);

        return this.messageService.createKakaoAlimtalkTemplate(request);
    }

    /**
     * 알림톡 템플릿 목록 조회 예제
     * @return KakaoAlimtalkTemplateListResponse
     */
    @GetMapping("get-alimtalk-templates")
    public KakaoAlimtalkTemplateListResponse getKakaoAlimtalkTemplates() {
        KakaoAlimtalkTemplateListRequest request = new KakaoAlimtalkTemplateListRequest();

        /// dateCreated, dateUpdated 등의 날짜 별 조회를 진행할 땐 아래와 같은 코드를 넣어주세요!
        // KakaoTemplateDateQuery.KakaoAlimtalkTemplateDateQueryCondition dateCreatedQueryCondition = KakaoTemplateDateQuery.KakaoAlimtalkTemplateDateQueryCondition.GREATER_THEN_OR_EQUAL;
        // KakaoTemplateDateQuery dateCreatedQuery = new KakaoTemplateDateQuery(Instant.parse("2025-09-01T00:00:00Z"), dateCreatedQueryCondition);
        // request.setDateCreated(dateCreatedQuery);

        /// status를 조회할 땐 KakaoAlimtalkTemplateStatus의 enum타입을 request.setStatus()에 넣어주세요!
        // request.setStatus(KakaoAlimtalkTemplateStatus.APPROVED); // 혹은 PENDING 등..

        /// 검색할 건 수, 값 미지정 시 20건 조회, 최대 500건 까지 설정 가능합니다.
        // request.setLimit(1);

        /// 조회 후 다음 페이지로 넘어가려면 이전에 조회할 당시 나왔던 nextKey 항목을 입력 해 주셔야 합니다!
        // request.setStartKey("조회 한 nextKey 데이터");

        /// 그 외 다른 조회 조건들은 <a href="https://developers.solapi.com/references/kakao/templates/getTemplateList">SOLAPI 개발문서 항목</a>을 참고해주세요!
        return this.messageService.getKakaoAlimtalkTemplates(request);
    }

    /**
     * 알림톡 템플릿 단 건 조회 예제
     * @return KakaoAlimtalkTemplateResponse
     */
    @GetMapping("get-alimtalk-template")
    public KakaoAlimtalkTemplateResponse getKakaoAlimtalkTemplate() {
        return this.messageService.getKakaoAlimtalkTemplate("조회 할 알림톡 템플릿 ID");
    }

    /**
     * 알림톡 템플릿 수정 예제
     * 알림톡 템플릿을 수정 할 때에는 channelId, channelGroupId를 넣으실 수 없습니다!
     * <a href="https://developers.solapi.com/references/kakao/templates/updateTemplate">알림톡 템플릿 수정 API 문서</a>
     * @return KakaoAlimtalkTemplateResponse
     */
    @PutMapping("update-alimtalk-template")
    public KakaoAlimtalkTemplateResponse updateKakaoAlimtalkTemplate() {
        KakaoAlimtalkTemplateMutationRequest request = new KakaoAlimtalkTemplateMutationRequest();
        request.setContent("수정할 알림톡 템플릿 내용");

        /// 그 외의 수정 조건은 <a href="https://developers.solapi.com/references/kakao/templates/updateTemplate">알림톡 템플릿 수정 API 문서</a>를 확인하시거나
        /// <a href="https://solapi.github.io/solapi-kotlin/solapi.sdk/com.solapi.sdk.message.dto.request.kakao/-kakao-alimtalk-template-mutation-request/index.html">SOLAPI Kotlin SDK 문서</a>
        /// 항목을 참고 해주세요!

        return this.messageService.updateKakaoAlimtalkTemplate("수정 할 알림톡 템플릿 ID", request);
    }

    /**
     * 알림톡 템플릿 이름 수정 예제
     * 이름을 수정 할 때에는 다른 알림톡 템플릿과 중복하여 수정하실 수 있습니다!
     * @return KakaoAlimtalkTemplateResponse
     */
    @PatchMapping("update-alimtalk-template-name")
    public KakaoAlimtalkTemplateResponse updateNameKakaoAlimtalkTemplate() {
        return this.messageService.updateKakaoAlimtalkTemplateName("이름을 수정 할 알림톡 템플릿 ID", "수정 할 이름");
    }

    /**
     * 알림톡 템플릿 검수요청 예제
     * @return KakaoAlimtalkTemplateResponse
     */
    @PatchMapping("inspection-request-alimtalk-template")
    public KakaoAlimtalkTemplateResponse inspectionRequestAlimtalkTemplate() {
        return this.messageService.requestKakaoAlimtalkTemplateInspection("검수 할 알림톡 템플릿 ID");
    }

    /**
     * 알림톡 템플릿 삭제 예제
     * @return KakaoAlimtalkTemplateResponse
     */
    @DeleteMapping("remove-alimtalk-template")
    public KakaoAlimtalkTemplateResponse removeAlimtalkTemplate() {
        return this.messageService.removeKakaoAlimtalkTemplate("삭제 할 알림톡 템플릿 ID");
    }
}
