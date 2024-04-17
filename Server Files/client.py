import socket
import threading

# Define server address and port
SERVER_ADDRESS = '127.0.0.1'  # Server's IP address
SERVER_PORT = 43434  # Server's port number

# Flag to check if it's the first message
first_message = True


def receive_messages(client_socket):
    global first_message
    try:
        while True:
            # Receive data from the server
            data = client_socket.recv(1024).decode('utf-8')
            if not data:
                break

            # Call first() function only for the first message
            if first_message:
                first_message = False
                first(data)

            print("Received from server:", data)
    except Exception as e:
        print("An error occurred:", e)
    finally:
        client_socket.close()


def send_messages(client_socket):
    try:
        while True:
            # Send message to the server
            message = input("Enter message to send to the chat room: ")
            client_socket.sendall(message.encode('utf-8'))
    except Exception as e:
        print("An error occurred:", e)
    finally:
        client_socket.close()


def first(data):
    data = data.split(',')
    terrainSize = data[0]
    terrainMaxHeight = data[1]
    waterSize = terrainSize


# Create a TCP socket
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

try:
    # Connect to the server
    client_socket.connect((SERVER_ADDRESS, SERVER_PORT))
    print("Connected to the chat room server.")

    # Start receiving and sending messages in separate threads
    receive_thread = threading.Thread(target=receive_messages, args=(client_socket,))
    receive_thread.start()

    send_thread = threading.Thread(target=send_messages, args=(client_socket,))
    send_thread.start()

    # Wait for threads to finish
    receive_thread.join()
    send_thread.join()

except ConnectionRefusedError:
    print("Connection to the server refused.")
except Exception as e:
    print("An error occurred:", e)
finally:
    # Close the socket
    client_socket.close()
