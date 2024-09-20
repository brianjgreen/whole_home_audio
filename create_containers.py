#
# sudo -E env PATH=${PATH} python3 create_container.py
#
# sudo docker run -d --restart unless-stopped --net host --device /dev/snd \
#     mikebrady/shairport-sync -a DenSystem -- -d hw:Set
#
import docker

shairport_image_name_tag = "mikebrady/shairport-sync:latest"
client = docker.from_env()

images = client.images.list()
print(images)

shairport_image = client.images.get(shairport_image_name_tag)
print(shairport_image)

new_container = client.containers.create(
    shairport_image_name_tag,
    detach=True,
    restart_policy={"Name": "unless-stopped"},
    network="host",
    devices=[
        "/dev/snd",
    ],
    command='-a PyRoom -- -d "hw:Set"',
)
print(new_container)
