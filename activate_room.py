#
# sudo -E env PATH=${PATH} python3 activate_room.py
#

import docker

client = docker.from_env()

for container in client.containers.list(all):
    # print(container.attrs)
    if "mikebrady/shairport-sync" in container.attrs["Config"]["Image"]:
        room = container.attrs["Config"]["Cmd"][1]
        usb_dongle = container.attrs["Config"]["Cmd"][-1]
        # print(f"{container.name} {container.status} {room} {usb_dongle}")
        state = "Not Connected"
        if container.attrs["State"]["Running"]:
            state = "ACTIVE"
        print(f"{room} on {usb_dongle} is {state}")
