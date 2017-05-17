from os import sys

def reversedSpiralOrder(length):

    #Initialize our four indexes
    top = 0
    down = length - 1
    left = 0
    right = length - 1
    result = ""

    while 1:
    
        # Print top row
        for j in range(left, right + 1):
            result += str(top * length + j) + ";"
        top += 1
        if top > down or left > right:
            break

        # Print the rightmost column
        for i in range(top, down + 1):
            result += str(i * length + right) + ";"
        right -= 1
        if top > down or left > right:
            break

        # Print the bottom row
        for j in range(right, left + 1, -1):
            result += str(down * length + j) + ";"
        down -= 1
        if top > down or left > right:
            break

        # Print the leftmost column
        for i in range(down, top + 1, -1):
            result += str(i * length + left) + ";"
        left += 1
        if top > down or left > right:
            break
                
    result = result.split(";")
    del result[-1]
    return result[::-1]

def canMove(x, y, direction, hull, map):
        
    # M = 1, B = 2
    moves = 0
    
    if direction == 0:
        y1 = -1
        y2 = -2
        hx1 = hx2 = x1 = x2 = 0
        hy1 = -y1 + hull
        hy2 = -y2 + hull
    elif direction == 1:
        x1 = 1
        x2 = 2
        hy1 = hy2 = y1 = y2 = 0
        hx1 = -x1 - hull
        hx2 = -x2 - hull
    elif direction == 2:
        y1 = 1
        y2 = 2
        hx1 = hx2 = x1 = x2 = 0
        hy1 = -y1 - hull
        hy2 = -y2 - hull
    elif direction == 3:
        x1 = -1
        x2 = -2
        hy1 = hy2 = y1 = y2 = 0
        hx1 = -x1 + hull
        hx2 = -x2 + hull
    
    if map[y + y1][x + x1] == "." and map[y + y2][x + x2] != "M":
        moves += 1

    if map[y + hy1][x + hx1] == "." and map[y + hy2][x + hx2] != "M":
        moves += 2

    return moves

if len(sys.argv) <= 1:
    f = open("PeaceMaker.txt","w") 
    f.write("")
    print "4"
else:
    arguments = sys.argv[1].split(";")
    sight = 19

    round = int(arguments[0])
    playerID = int(arguments[1])
    x = int(arguments[2].split(",")[0])
    y = int(arguments[2].split(",")[1])
    direction = int(arguments[2].split(",")[2])
    hull = arguments[3]
    moves = int(arguments[4].split(",")[0])
    shots = int(arguments[4].split(",")[1])
    mines = int(arguments[4].split(",")[2])
    cooldown = int(arguments[4].split(",")[3])
    hits = int(arguments[5].split(",")[0])
    kills = int(arguments[5].split(",")[0])
    taken = int(arguments[5].split(",")[0])
    underwater = int(arguments[6].split(",")[0])
    shield = int(arguments[6].split(",")[1])
    scan = int(arguments[6].split(",")[2])
    map = [[list(arguments[7])[j * sight + i] for i in xrange(sight)] for j in xrange(sight)]
        
    initialShots = shots
        
        
    priorities = reversedSpiralOrder(sight)

    actions = ""
    sighted = 0
    for priority in priorities:
        pX = int(priority) % sight
        pY = int(priority) / sight
        
        if map[pY][pX] == "A":
            sighted += 1
            if shots > 0:
                shots -= 1
                actions += "F" + ("+" if pX - 9 >= 0 else "") + str(pX - 9)  + ("+" if pY - 9 >= 0 else "") + str(pY - 9) 

    if shots == initialShots and sighted > 0:
        actions += "D"
    elif shots == initialShots and sighted <= 0:
        actions += "S"
    else:
        actions += ""
        
    f = open("PeaceMaker.txt","r") 
    fC = f.read(1)
    lastDirection = int("1" if fC == "" else fC)
            
    y = 9
    x = 9

    if lastDirection == 1:
        if canMove(x, y, direction, len(hull), map) == 1 or canMove(x, y, direction, len(hull), map) == 3:
            actions += "M"
        elif canMove(x, y, direction, len(hull), map) == 2:
            actions += "B"
            lastDirection = 0
    elif lastDirection == 0:
        if canMove(x, y, direction, len(hull), map) == 2 or canMove(x, y, direction, len(hull), map) == 3:
            actions += "B"
        elif canMove(x, y, direction, len(hull), map) == 1:
            actions += "M"
            lastDirection = 1
    
    f = open("PeaceMaker.txt","w")
    f.write(str(lastDirection))
    
    print actions