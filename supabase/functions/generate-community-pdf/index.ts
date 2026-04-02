import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { jsPDF } from "https://esm.sh/jspdf@2.5.1"
import { KOREAN_FONT_BASE64 } from "./font.ts"


serve(async (req) => {
  try {
    const { postId, title, content, category, author, extra_info } = await req.json()

    // 1. PDF 객체 생성 (기본 단위: mm)
    const doc = new jsPDF();

    // 2. 한글 폰트 등록 및 설정
    // vfs(가상 파일 시스템)에 폰트를 추가하고 적용합니다.
    doc.addFileToVFS("NanumGothic.ttf", KOREAN_FONT_BASE64);
    doc.addFont("NanumGothic.ttf", "NanumGothic", "normal");
    doc.setFont("NanumGothic");

    // 3. 제목 작성
    doc.setFontSize(22);
    doc.setTextColor(49, 130, 246); // 토스 블루 (#3182F6)
    doc.text(`[${category}] ${title}`, 20, 25);

    // 4. 작성자 정보
    doc.setFontSize(10);
    doc.setTextColor(139, 149, 161); // 토스 그레이 (#8B95A1)
    doc.text(`작성자: ${author} | 생성일: ${new Date().toLocaleDateString('ko-KR')}`, 20, 35);

    // 5. 구분선
    doc.setDrawColor(209, 214, 219);
    doc.line(20, 40, 190, 40);

    // 6. 본문 내용 (줄바꿈 처리)
    doc.setFontSize(12);
    doc.setTextColor(51, 61, 75);
    const splitContent = doc.splitTextToSize(content, 170);
    doc.text(splitContent, 20, 50);

    // 7. 추가 정보 (extra_data) 섹션
    let currentY = 50 + (splitContent.length * 7) + 10;

    doc.setFillColor(242, 244, 246); // 배경색 (#F2F4F6)
    doc.rect(20, currentY, 170, (extra_info.length * 10) + 10, 'F');

    doc.setFontSize(11);
    doc.text("■ 추가 상세 정보", 25, currentY + 8);

    currentY += 15;
    extra_info.forEach((item: any) => {
      doc.setFontSize(10);
      doc.setTextColor(139, 149, 161);
      doc.text(item.label, 25, currentY); // 라벨 (왼쪽)

      doc.setTextColor(51, 61, 75);
      doc.text(item.value, 185, currentY, { align: "right" }); // 값 (오른쪽 정렬)
      currentY += 8;
    });

    // 8. PDF를 바이너리 데이터로 변환
    const pdfOutput = doc.output("arraybuffer");

    return new Response(pdfOutput, {
      headers: {
        "Content-Type": "application/pdf",
        "Content-Disposition": `attachment; filename="${encodeURIComponent(title)}.pdf"`
      },
    })

  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    })
  }
})