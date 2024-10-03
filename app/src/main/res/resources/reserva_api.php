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

// Variables de entrada
$id = isset($request[1]) ? intval($request[1]) : null;

switch ($method) {
    case 'POST': // Insertar una reserva
        $data = json_decode(file_get_contents('php://input'), true);
        insertarReserva($conn, $data['id_maestro'], $data['fecha_visita'], $data['coste'], $data['ciudad'], $data['id_usuario']);
        break;

    case 'GET':
        if ($id) { // Obtener reserva por ID
            buscarReservaPorId($conn, $id);
        } else { // Obtener todas las reservas
            obtenerReservas($conn);
        }
        break;

    case 'PUT': // Actualizar una reserva por ID
        if ($id) {
            $data = json_decode(file_get_contents('php://input'), true);
            actualizarReservaPorId($conn, $data['id_maestro'], $data['fecha_visita'], $data['coste'], $data['ciudad'], $data['id_usuario'], $id);
        }
        break;

    case 'DELETE': // Eliminar una reserva por ID
        if ($id) {
            eliminarReserva($conn, $id);
        }
        break;

    default:
        http_response_code(405); // Método no permitido
        echo json_encode(["mensaje" => "Método no permitido"]);
        break;
}

$conn->close();

// Funciones CRUD

function insertarReserva($conn, $id_maestro, $fecha_visita, $coste, $ciudad, $id_usuario) {
    $sql = "INSERT INTO RESERVA (id_maestro, fecha_visita, coste, ciudad, id_usuario) VALUES (?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("isdsi", $id_maestro, $fecha_visita, $coste, $ciudad, $id_usuario);
    if ($stmt->execute()) {
        http_response_code(201); // Creado
        echo json_encode(["mensaje" => "Reserva insertada correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}

function eliminarReserva($conn, $id) {
    $sql = "DELETE FROM RESERVA WHERE id_reserva = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    if ($stmt->execute()) {
        http_response_code(200); // Eliminado
        echo json_encode(["mensaje" => "Reserva eliminada correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}

function obtenerReservas($conn) {
    $sql = "SELECT * FROM RESERVA";
    $result = $conn->query($sql);
    $reservas = [];
    if ($result->num_rows > 0) {
        while($row = $result->fetch_assoc()) {
            $reservas[] = $row;
        }
        echo json_encode($reservas);
    } else {
        echo json_encode(["mensaje" => "No se encontraron reservas"]);
    }
}

function buscarReservaPorId($conn, $id) {
    $sql = "SELECT * FROM RESERVA WHERE id_reserva = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        $reserva = $result->fetch_assoc();
        echo json_encode($reserva);
    } else {
        http_response_code(404); // No encontrado
        echo json_encode(["mensaje" => "No se encontró ninguna reserva con ese ID"]);
    }
    $stmt->close();
}

function actualizarReservaPorId($conn, $id_maestro, $fecha_visita, $coste, $ciudad, $id_usuario, $id) {
    $sql = "UPDATE RESERVA SET id_maestro = ?, fecha_visita = ?, coste = ?, ciudad = ?, id_usuario = ? WHERE id_reserva = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("isdsii", $id_maestro, $fecha_visita, $coste, $ciudad, $id_usuario, $id);
    if ($stmt->execute()) {
        echo json_encode(["mensaje" => "Reserva actualizada correctamente"]);
    } else {
        http_response_code(400); // Error de solicitud
        echo json_encode(["error" => $stmt->error]);
    }
    $stmt->close();
}
?>
