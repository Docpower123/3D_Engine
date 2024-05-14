import socket
import threading
import time
import random
import math

# List to keep track of connected clients and their positions
clients = []
client_positions = {}  # Dictionary to store client positions
attack_req = {}

# Function to create the world data
def create_world():
    world = []
    terrain_size = 20000
    world.append(terrain_size)
    world.append(2000)

    # create lights entities
    for i in range(2, 200, 2):
        world.append(random.uniform(-terrain_size / 2, terrain_size / 2))
        world.append(random.uniform(-terrain_size / 2, terrain_size / 2))

    # create entities
    for i in range(201, 50000, 6):
        world.append(random.choice(
            ["treeModel", "lowPolyTreeModel", "pineModel", "grassModel", "flowerModel", "fernModel", "toonRocksModel"]))
        world.append(random.uniform(-terrain_size / 2, terrain_size / 2))
        world.append(random.uniform(-terrain_size / 2, terrain_size / 2))
        world.append(abs(random.random()))
        world.append(abs(random.random()))
        world.append(random.uniform(0.7, 3))
    return world

def handle_client(client_socket, addr):
    print("Connection from", addr)
    try:
        # Send world data to the client
        world_data = create_world()
        client_socket.sendall(bytes(str(world_data) + '\n', 'utf-8'))
        time.sleep(1)  # Introduce a delay for demonstration

        # Add client to the list of connected clients
        clients.append((client_socket, addr))

        while True:
            data = client_socket.recv(1024)
            if not data:
                break

            decoded_data = data.decode('utf-8')  # Ensure correct decoding
            if len(decoded_data.split('*')) == 3:
                position_data, health, attack = decoded_data.split('*')
                attack = attack.strip().lower()
                if attack == 'true':
                    attack_req[addr] = attack
                elif attack == 'false' and addr in attack_req:
                    del attack_req[addr]  # Remove attack entry if no longer attacking

                position = position_data.split(';')  # Assuming format: 'ip;port;position'
                client_positions[addr] = (position, health)  # Store position and health
                update_positions()
    except Exception as e:
        print("Error handling client:", e)
    finally:
        # Remove client from the list and their position
        if (client_socket, addr) in clients:
            clients.remove((client_socket, addr))
        if addr in client_positions:
            client_positions.pop(addr, None)
        if addr in attack_req:
            attack_req.pop(addr, None)
        client_socket.close()
        print("Connection closed with", addr)

def distance(pos1, pos2):
    # Calculate Euclidean distance between two 3D points
    return math.sqrt((pos1[0] - pos2[0]) ** 2 + (pos1[1] - pos2[1]) ** 2 + (pos1[2] - pos2[2]) ** 2)

def handle_attacks():
    attack_radius = 30  # Define your attack radius here
    damage_amount = -100  # Define the damage amount here

    # Process attacks
    for addr1, flag in list(attack_req.items()):  # Use list to avoid runtime dictionary change
        standardized_flag = flag.strip().lower()  # Remove whitespaces and convert to lower case
        if standardized_flag == 'true':
            attacker_data = client_positions.get(addr1)
            if attacker_data:
                pos1_str, health1 = attacker_data
                try:
                    # Convert position string to list of floats
                    pos1 = [float(x) for x in pos1_str[0].split(',')]
                    print(f"Attacker {addr1} position: {pos1}, Health: {health1}")
                except ValueError:
                    print(f"Invalid position value for attacker {addr1}: {pos1_str}")
                    continue

                for addr, (pos_str, health) in client_positions.items():
                    if addr != addr1:
                        try:
                            # Convert position string to list of floats
                            pos2 = [float(x) for x in pos_str[0].split(',')]
                            print(f"Target {addr} position: {pos2}, Health: {health}")
                        except ValueError:
                            print(f"Invalid position value for target {addr}: {pos_str}")
                            continue

                        if distance(pos1, pos2) <= attack_radius:
                            try:
                                new_health = int(health) + damage_amount  # Perform arithmetic with integers
                                if new_health < 0:  # Ensure health doesn't go below 0
                                    new_health = 0
                                client_positions[addr] = (pos_str, str(new_health))  # Convert back to string
                                print(f"Client {addr} attacked by {addr1}! New health: {new_health}")
                                if new_health == 0:
                                    send_death_message(addr)
                                    broadcast_message(f"PLAYER_KILLED {addr}")
                                    remove_client(addr)
                            except ValueError:
                                print(f"Invalid health value for client {addr}: {health}")

            # Reset the attack flag
            attack_req[addr1] = 'false'

def send_death_message(addr):
    for client, client_addr in clients:
        if client_addr == addr:
            try:
                client.sendall(b'You have been killed.\n')
            except Exception as e:
                print(f"Error sending death message to {addr}:", e)

def broadcast_message(message):
    for client, _ in clients:
        try:
            client.sendall(bytes(message + '\n', 'utf-8'))
        except Exception as e:
            print(f"Error broadcasting message: {e}")

def remove_client(addr):
    for client, client_addr in clients:
        if client_addr == addr:
            client.close()
            clients.remove((client, addr))
            break
    if addr in client_positions:
        client_positions.pop(addr, None)
    if addr in attack_req:
        attack_req.pop(addr, None)
    print(f"Client {addr} has been removed.")

def update_positions():
    handle_attacks()  # Process all attacks before sending updates
    for client, addr in clients:
        # Create a formatted string of all positions and health data excluding the client's own data
        positions_data = '/p'.join(f"{other_addr}; {pos}*{health}"
                                   for other_addr, (pos, health) in client_positions.items()
                                   if other_addr != addr)
        encoded_data = bytes(positions_data + '\n', 'utf-8')
        try:
            client.sendall(encoded_data)
        except Exception as e:
            print(f"Error broadcasting positions to {addr}:", e)

# Function to start the server
def server():
    host = '192.168.1.164'
    port = 5005
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((host, port))
    server_socket.listen(5)
    print("Server listening on port", port)

    while True:
        client_socket, addr = server_socket.accept()
        client_thread = threading.Thread(target=handle_client, args=(client_socket, addr))
        client_thread.start()

if __name__ == "__main__":
    server()
