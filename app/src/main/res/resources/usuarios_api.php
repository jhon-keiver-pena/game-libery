<?php
header('Content-Type: application/json'); // Establecer el encabezado para JSON

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "APP_MOBILE_MASTER";

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die(json_encode(["error" => "Conexión fallida: " . $conn->connect_error]));
}

// Obtener el método HTTP
$method = $_SERVER['REQUEST_METHOD'];
$request = explode('/', trim($_SERVER['PATH_INFO'], '/'));
$id = isset($request[1]) ? intval($request[1]) : null;

switch ($method) {
    case 'POST': // Insertar un usuario
        $data = json_decode(file_get_contents('php://input'), true);
        insertarUsuario($conn, $data['nombre'], $data['apellido'], $data['telefono'], $data['correo'], $data['clave']);
        break;

    case 'GET':
        // Buscar usuario por correo y clave
        if (isset($_GET['correo']) && isset($_GET['clave'])) { 
            $correo = $_GET['correo'];
            $clave = $_GET['clave'];
            buscarUsuarioPorCorreoYClave($conn, $correo, $clave);
        } else {
            http_response_code(400); // Bad request
            echo json_encode(["mensaje" => "Se requieren ambos parámetros: correo y clave."]);
        }
        break;

    case 'PUT': // Actualizar un usuario por ID
        if ($id) {
            $data = json_decode(file_get_contents('php://input'), true);
            actualizarUsuarioPorId($conn, $id, $data['nombre'], $data['apellido'], $data['telefono'], $data['correo'], $data['clave']);
        }
        break;

    case 'DELETE': // Eliminar un usuario por ID
        if ($id) {
            eliminarUsuario($conn, $id);
        }
        break;

    default:
        http_response_code(405); // Método no permitido
        echo json_encode(["mensaje" => "Método no permitido"]);
        break;
}

$conn->close();

// Funciones CRUD

function insertarUsuario($conn, $nombre, $apellido, $telefono, $correo, $clave) {
    $sql = "INSERT INTO USUARIO (nombre, apellido, telefono, correo, clave) VALUES (?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sssss", $nombre, $apellido, $telefono, $correo, $clave);
    if ($stmt->execute()) {
        http_response_code(201); // Creado
        echo json_encode(["mensaje" => "Usuario insertado correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}

function buscarUsuarioPorCorreoYClave($conn, $correo, $clave) {
    $sql = "SELECT * FROM USUARIO WHERE correo = ? AND clave = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $correo, $clave);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        $usuario = $result->fetch_assoc();
        echo json_encode($usuario);
    } else {
        http_response_code(404); // No encontrado
        echo json_encode(["mensaje" => "No se encontró ningún usuario con ese correo y clave"]);
    }
    $stmt->close();
}

function actualizarUsuarioPorId($conn, $id, $nombre, $apellido, $telefono, $correo, $clave) {
    $sql = "UPDATE USUARIO SET nombre = ?, apellido = ?, telefono = ?, correo = ?, clave = ? WHERE id_usuario = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sssssi", $nombre, $apellido, $telefono, $correo, $clave, $id);
    if ($stmt->execute()) {
        echo json_encode(["mensaje" => "Usuario actualizado correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}

function eliminarUsuario($conn, $id) {
    $sql = "DELETE FROM USUARIO WHERE id_usuario = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    if ($stmt->execute()) {
        http_response_code(200); // Eliminado
        echo json_encode(["mensaje" => "Usuario eliminado correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}
?>
