package sever.handler;

import java.io.ObjectOutputStream;

public interface AuthHandler {
  // Nhận mảng dữ liệu và stream để phản hồi object
  void handle(String[] parts, ObjectOutputStream out);
}