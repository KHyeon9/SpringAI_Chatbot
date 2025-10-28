package com.education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * EmbedingService
 *
 * 업로드된 PDF 파일을 읽어 텍스트를 추출하고,
 * 이를 청크 단위로 분할(Token Split) 후 벡터로 변환하여
 * Vector Store(PGVector 등)에 저장하는 역할을 수행.
 *
 * RAG(Retrieval-Augmented Generation)의 "R" 부분인
 * 데이터 인덱싱 과정을 담당하는 핵심 서비스.
 */
@Service
@RequiredArgsConstructor
public class EmbedingService {

    private final VectorStore vectorStore;

    /**
     * 사용자가 업로드한 PDF 파일을 벡터화(임베딩)하여 Vector Store에 저장하는 메서드.
     */
    public void processUploadPdf(MultipartFile file) throws IOException {
        // 1. 업로드된 MultipartFile을 임시 파일로 저장
        File tempFile = File.createTempFile("uploaded", "pdf");
        file.transferTo(tempFile);
        Resource fileResource = new FileSystemResource(tempFile);
        try {
            // 2. PDF 문서 읽기 설정 구성
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageBottomMargin(0) // PDF 상단 여백 설정
                    .withPageExtractedTextFormatter( // 페이지에서 추출된 Text를 포맷팅 방식 지정
                            ExtractedTextFormatter
                                    .builder()
                                    .withNumberOfBottomTextLinesToDelete(0) // 페이지 상단에서 삭제할 텍스트 수
                                    .build()
                    )
                    .withPagesPerDocument(1) // 페이지에서 한번에 처리할 수 있는 페이지 지정
                    .build();

            // 3. PDF 문서 읽기 (페이지 단위)
            PagePdfDocumentReader pdfReader =
                    new PagePdfDocumentReader(fileResource, config);
            // PDF에서 텍스트를 추출해 Document 리스트로 변환
            List<Document> documents = pdfReader.get();

            // 4. 텍스트를 청크 단위로 분할
            /*
             * TokenTextSplitter는 문서를 토큰 단위로 분리하여
             * LLM 임베딩에 적합한 크기로 나누는 역할을 함.
             *
             * - chunkSize: 기본 청크 크기 (토큰 단위)
             * - minChunkSizeChars: 최소 청크 크기 (문자 단위)
             * - minChunkLengthToEmbed: 임베딩 수행 최소 길이
             * - maxNumChunks: 최대 청크 개수
             * - keepSeparator: 분리 기호 유지 여부
             */
            TokenTextSplitter splitter = new TokenTextSplitter(
                    1000, // chunkSize
                    400,  // minChunkSizeChars
                    10,   // minChunkLengthToEmbed
                    5000, // maxNumChunks
                    true  // keepSeparator
            );
            List<Document> splitDocuments = splitter.apply(documents);

            // 5. 분할된 문서 청크를 Vector Store에 저장 (임베딩 생성 + 인덱싱)
            vectorStore.accept(splitDocuments);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 임시 파일 삭제
            tempFile.delete();
        }
    }
}
