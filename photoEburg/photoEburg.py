# coding:utf8
__author__ = 'Иван'

import vk
import os
import time
import json
import argparse
import requests
import urllib.request

REDIRECT_URI = 'https://oauth.vk.com/blank.html'

def main():
    parser = argparse.ArgumentParser(description='Use this script for import photo to Eburg')
    parser.add_argument("token",
                    type=str,
                    help="Access token for application")
    parser.add_argument("fromGroup",
                        type=int,
                        help="Идентификатор группы, из которой хотите импортировать фотографии")
    parser.add_argument("fromAlbum",
                        type=int,
                        help="Идентификатор(-ы) альбома(-ов), из которого(-ых) хотите импортировать фотографии")
    parser.add_argument("toGroup",
                        type=int,
                        # required=false,
                        help="Идентификатор группы, в которую хотите экспортировать фотографии")
    parser.add_argument("toAlbum",
                        type=int,
                        # required=false,
                        help="Идентификатор альбома, в который хотите экспортировать фотографии")
    args = parser.parse_args()
    my_app_id = '5117295'

    vkapi = vk.API(access_token=args.token)

    bal = vkapi.groups.search(q="Балы УрФУ", count=1)['items'][0]
    albumFrom = vkapi.photos.getAlbums(owner_id=args.fromGroup, album_ids=args.fromAlbum)['items'][0]
    print(albumFrom)

    albumTo = vkapi.photos.getAlbums(owner_id=args.toGroup, album_ids=args.toAlbum)['items'][0]
    print(albumTo)
    #
    # for photo in vkapi.photos.get(owner_id='-'+str(bal['id']), album_id=albumFrom['id'])['items']:
    #      try:
    #          time.sleep(1)
    #          vkapi.photos.copy(owner_id='-'+str(bal['id']), photo_id=photo['id'])
    #          print(photo)
    #      except Exception as e:
    #          print(e)

    for photo in vkapi.photos.get(owner_id=args.fromGroup, album_id=args.fromAlbum, photo_sizes=1)['items']:
        try:
            time.sleep(1)
            sizez = filter(lambda size: size['type'] == 'z', photo['sizes'])
            urllib.request.urlretrieve(sizez.__next__()['src'], str(photo['id'])+'.png')
            img = {'photo': (str(photo['id'])+'.png', open(str(photo['id'])+'.png', 'rb'))}

            urlpost = vkapi.photos.getUploadServer(group_id=-albumTo['owner_id'], album_id=albumTo['id'])['upload_url']
            Resp = requests.post(urlpost, files=img)
            response = json.loads(Resp.text)

            vkapi.photos.save(
                group_id=response['gid'],
                server=response['server'],
                photos_list=response['photos_list'],
                album_id=response['aid'],
                hash=response['hash']
            )
            print(photo)
        except Exception as e:
            print(e)

    for photo in os.listdir(os.get_exec_path()):
        print(photo)



if __name__ == '__main__':
    main()