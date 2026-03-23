# QUY ĐỊNH LÀM VIỆC NHÓM & GIT WORKFLOW

## 1. Quy trình dùng Git (BẮT BUỘC)
1. **Tuyệt đối KHÔNG push thẳng lên nhánh `main`.** Nhánh `main` chỉ chứa code hoàn chỉnh, chạy không lỗi.
2. Khi bắt đầu làm 1 tính năng mới (Ví dụ: Làm màn hình Login):
   * Từ nhánh `main`, tạo nhánh mới: `git checkout -b feature/login`
   * Code và commit trên nhánh này: `git commit -m "Thiết kế UI Login"`
   * Đẩy nhánh lên Github: `git push origin feature/login`
   * Tạo **Pull Request (PR)** trên GitHub. Báo cho Leader review. Leader duyệt mới được merge vào `main`.

## 2. Quy tắc viết Code (Coding Convention - Java)
*   **Tên Class/Model:** Viết hoa chữ cái đầu (PascalCase). VD: `KhoanThuModel`, `LoginController`.
*   **Tên biến/Hàm:** Viết thường chữ cái đầu (camelCase). VD: `maKhoanThu`, `tinhTongTien()`.
*   **Căn lề:** Dùng 4 khoảng trắng (hoặc 1 tab). Không để dòng code dài quá 80 ký tự.
*   **Comment Code:** Sử dụng `/** Javadoc */` trước các hàm quan trọng để giải thích chức năng của hàm đó.
