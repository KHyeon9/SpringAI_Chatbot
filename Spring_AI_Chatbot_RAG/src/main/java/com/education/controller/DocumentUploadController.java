package com.education.controller;

import com.education.service.EmbedingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentUploadController {

    private final ChatModel chatModel;
    private final EmbedingService embedingService;
    private final VectorStore vectorStore;

    @PostMapping("/upload")
    public ResponseEntity<String> upLoadPdf(@RequestParam("file") MultipartFile file) {
        try {
            embedingService.processUploadPdf(file);
            return ResponseEntity.ok("PDF 업로드 및 임베딩 완료");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("처리중 오류 발생 : " + e.getMessage());
        }
    }

    // "회사" 라는 단어를 벡터로 임베딩하고 값을 확인 및 테스트 하기 위해 작성
    @PostMapping("/save")
    public ResponseEntity<String> embedAndStore(@RequestParam String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("텍스트를 입력해 주세요.");
            }

            // 1. Document 객체 생성
            Document document = new Document(text);

            // 2. 선택적인 메타데이터 추가
            document.getMetadata().put("source", "embedding-api");

            // 3. List<Document> 형태로 감싸서 저장
            vectorStore.accept(List.of(document));

            return ResponseEntity.ok("입력한 문장이 백터 DB에 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("저장 중에 오류 발생 : " + e.getMessage());
        }
    }

    private String promptTemplate = """
            문서를 참고하여 질문에 대한 구체적인 문장으로 답변해 주세요.
            문서에서 찾을 수 없다면 "관련 정보를 찾을 수 없습니다."라고 답변해 주세요.
            
            [문서]
            {context}
            
            [질문]
            {question}
            
            """;

    @PostMapping("/rag")
    public String ragChat(@RequestParam String question) {

        PromptTemplate template = new PromptTemplate(promptTemplate);

        Map<String, Object> promptParamiters = new HashMap<>();
        promptParamiters.put("question", question);

        // 1. VectorStore에서 유사도 높은 문서 2개 검색
        List<Document> similarityDocuments =
                vectorStore.similaritySearch(
                        SearchRequest
                                .builder()
                                .query(question)
                                .topK(4)
                                .build()
                );

        // 2. 검색된 문서 내용을 하나의 문자열로 결합
        String documents = similarityDocuments
                                .stream()
                                .map(Document::getText)
                                .collect(Collectors.joining("\n")
                            );

        promptParamiters.put("context", documents);

        return chatModel
                .call(template.create(promptParamiters))
                .getResult()
                .getOutput()
                .getText();
    }
}
