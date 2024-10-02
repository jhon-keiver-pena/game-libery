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
    die("Conexión fallida: " . $conn->connect_error);
}

// Obtener el método HTTP
$method = $_SERVER['REQUEST_METHOD'];
$request = explode('/', trim($_SERVER['PATH_INFO'], '/'));

$id = isset($request[1]) ? intval($request[1]) : null;

switch ($method) {
    case 'POST': // Insertar un maestro
        $data = json_decode(file_get_contents('php://input'), true);
        insertarMaestro($conn, $data['nombre'], $data['telefono'], $data['edad'], $data['sexo'], $data['experiencia'], $data['tiempo_campo'], $data['especialidad'], $data['url_imagen']);
        break;

    case 'GET':
        if ($id) { // Obtener maestro por ID
            buscarMaestroPorId($conn, $id);
        } else { // Obtener todos los maestros
            obtenerMaestros($conn);
        }
        break;

    case 'PUT': // Actualizar un maestro por ID
        if ($id) {
            $data = json_decode(file_get_contents('php://input'), true);
            actualizarMaestroPorId($conn, $data['nombre'], $data['telefono'], $data['edad'], $data['sexo'], $data['experiencia'], $data['tiempo_campo'], $data['especialidad'], $data['url_imagen'], $id);
        }
        break;

    case 'DELETE': // Eliminar un maestro por ID
        if ($id) {
            eliminarMaestro($conn, $id);
        }
        break;

    default:
        http_response_code(405); // Método no permitido
        echo json_encode(["mensaje" => "Método no permitido"]);
        break;
}

$conn->close();

// Funciones CRUD

function insertarMaestro($conn, $nombre, $telefono, $edad, $sexo, $experiencia, $tiempo_campo, $especialidad, $url_imagen) {
    $sql = "INSERT INTO MAESTRO (nombre, telefono, edad, sexo, experiencia, tiempo_campo, especialidad, url_imagen) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssisssss", $nombre, $telefono, $edad, $sexo, $experiencia, $tiempo_campo, $especialidad, $url_imagen);
    if ($stmt->execute()) {
        http_response_code(201); // Creado
        echo json_encode(["mensaje" => "Maestro insertado correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}

function eliminarMaestro($conn, $id) {
    $sql = "DELETE FROM MAESTRO WHERE id_maestro = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    if ($stmt->execute()) {
        http_response_code(200); // Eliminado
        echo json_encode(["mensaje" => "Maestro eliminado correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}

function obtenerMaestros($conn) {
    $sql = "SELECT * FROM MAESTRO";
    $result = $conn->query($sql);
    $maestros = [];
    if ($result->num_rows > 0) {
        while($row = $result->fetch_assoc()) {
            $maestros[] = $row;
        }
        echo json_encode($maestros);
    } else {
        echo json_encode(["mensaje" => "No se encontraron maestros"]);
    }
}

function buscarMaestroPorId($conn, $id) {
    $sql = "SELECT * FROM MAESTRO WHERE id_maestro = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        $maestro = $result->fetch_assoc();
        echo json_encode($maestro);
    } else {
        http_response_code(404); // No encontrado
        echo json_encode(["mensaje" => "No se encontró ningún maestro con ese ID"]);
    }
    $stmt->close();
}

function actualizarMaestroPorId($conn, $nombre, $telefono, $edad, $sexo, $experiencia, $tiempo_campo, $especialidad, $url_imagen, $id) {
    $sql = "UPDATE MAESTRO SET nombre = ?, telefono = ?, edad = ?, sexo = ?, experiencia = ?, tiempo_campo = ?, especialidad = ?, url_imagen = ? WHERE id_maestro = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssisssssi", $nombre, $telefono, $edad, $sexo, $experiencia, $tiempo_campo, $especialidad, $url_imagen, $id);
    if ($stmt->execute()) {
        echo json_encode(["mensaje" => "Maestro actualizado correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}
?>
