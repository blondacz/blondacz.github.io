#!/bin/bash

# Prompt user for password securely (input will not be visible)
read -s -p "Enter SSH password: " PASSWORD
echo ""

USER="your_ssh_user"
COMMAND="your_command_here"  # Example: "hostname && uptime"
OUTPUT_FILE="results.txt"
HOSTS_FILE="hosts.txt"

# Clear previous output file
> "$OUTPUT_FILE"

# Loop through each host
while IFS= read -r HOST; do
    echo "Running command on $HOST..."
    sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no "$USER@$HOST" "$COMMAND" >> "$OUTPUT_FILE" 2>&1
    echo "----------------------------------------" >> "$OUTPUT_FILE"
done < "$HOSTS_FILE"

echo "All commands executed. Results saved in $OUTPUT_FILE."
