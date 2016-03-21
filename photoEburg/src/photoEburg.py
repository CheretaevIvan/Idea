# coding:utf8
__author__ = 'Иван'

import vk
import os
import time
import json
import argparse
import requests
import glob

import http.cookiejar as cookielib
import urllib.request as urllib2
import urllib
from urllib.parse import urlparse
from html.parser import HTMLParser


class FormParser(HTMLParser):
    def __init__(self):
        HTMLParser.__init__(self)
        self.url = None
        self.params = {}
        self.in_form = False
        self.form_parsed = False
        self.method = "GET"

    def handle_starttag(self, tag, attrs):
        tag = tag.lower()
        if tag == "form":
            if self.form_parsed:
                raise RuntimeError("Second form on page")
            if self.in_form:
                raise RuntimeError("Already in form")
            self.in_form = True
        if not self.in_form:
            return
        attrs = dict((name.lower(), value) for name, value in attrs)
        if tag == "form":
            self.url = attrs["action"]
            if "method" in attrs:
                self.method = attrs["method"].upper()
        elif tag == "input" and "type" in attrs and "name" in attrs:
            if attrs["type"] in ["hidden", "text", "password"]:
                self.params[attrs["name"]] = attrs["value"] if "value" in attrs else ""

    def handle_endtag(self, tag):
        tag = tag.lower()
        if tag == "form":
            if not self.in_form:
                raise RuntimeError("Unexpected end of <form>")
            self.in_form = False
            self.form_parsed = True

def auth(email, password, client_id, scope):
    def split_key_value(kv_pair):
        kv = kv_pair.split("=")
        return kv[0], kv[1]

    # Authorization form
    def auth_user(email, password, client_id, scope, opener):
        response = opener.open(
            "http://oauth.vk.com/oauth/authorize?" + \
            "redirect_uri=http://oauth.vk.com/blank.html&response_type=token&" + \
            "client_id=%s&scope=%s&display=wap" % (client_id, ",".join(scope))
        )
        doc = response.read()
        parser = FormParser()
        parser.feed(str(doc))
        parser.close()
        if not parser.form_parsed or parser.url is None or "pass" not in parser.params or \
                        "email" not in parser.params:
            raise RuntimeError("Something wrong")
        parser.params["email"] = email
        parser.params["pass"] = password
        if parser.method == "POST":
            response = opener.open(parser.url, urllib.parse.urlencode(parser.params).encode())
        else:
            raise NotImplementedError("Method '%s'" % parser.method)
        return response.read(), response.geturl()

    # Permission request form
    def give_access(doc, opener):
        parser = FormParser()
        parser.feed(str(doc))
        parser.close()
        if not parser.form_parsed or parser.url is None:
            raise RuntimeError("Something wrong")
        if parser.method == "POST":
            response = opener.open(parser.url, urllib.parse.urlencode(parser.params).encode())
        else:
            raise NotImplementedError("Method '%s'" % parser.method)
        return response.geturl()


    if not isinstance(scope, list):
        scope = [scope]
    opener = urllib2.build_opener(
        urllib2.HTTPCookieProcessor(cookielib.CookieJar()),
        urllib2.HTTPRedirectHandler())
    doc, url = auth_user(email, password, client_id, scope, opener)
    if urlparse(url).path != "/blank.html":
        # Need to give access to requested scope
        url = give_access(doc, opener)
    if urlparse(url).path != "/blank.html":
        raise RuntimeError("Expected success here")
    answer = dict(split_key_value(kv_pair) for kv_pair in urlparse(url).fragment.split("&"))
    if "access_token" not in answer or "user_id" not in answer:
        raise RuntimeError("Missing some values in answer")
    return answer["access_token"], answer["user_id"]

REDIRECT_URI = 'https://oauth.vk.com/blank.html'

def main():
    parser = argparse.ArgumentParser(description='Use this script for import photo to Eburg')
    parser.add_argument("login",
                        type=str,
                        help="Логин от аккаунта ВКонтакте")
    parser.add_argument("password",
                        type=str,
                        help="Пароль от аккаунта ВКонтакте")
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

    token, user_id = auth(args.login, args.password, my_app_id, "photos")
    vkapi = vk.API(access_token=token)
    albumFrom = vkapi.photos.getAlbums(owner_id=args.fromGroup, album_ids=args.fromAlbum)['items'][0]

    albumTo = vkapi.photos.getAlbums(owner_id=args.toGroup, album_ids=args.toAlbum)['items'][0]

    for photo in vkapi.photos.get(owner_id=args.fromGroup, album_id=args.fromAlbum, photo_sizes=1)['items']:
        try:
            time.sleep(1)
            sizez = filter(lambda size: size['type'] == 'z', photo['sizes'])
            urllib.request.urlretrieve(sizez.__next__()['src'], str(photo['id'])+'.png')
            img = {'photo': (str(photo['id'])+'.png', open(str(photo['id'])+'.png', 'rb'))}

            urlpost = vkapi.photos.getUploadServer(group_id=-albumTo['owner_id'], album_id=albumTo['id'])['upload_url']
            Resp = requests.post(urlpost, files=img)
            response = json.loads(Resp.text)
            print(response)
            #vkapi.photos.save(
                #group_id=response['gid'],
                #server=response['server'],
                #photos_list=response['photos_list'],
                #album_id=response['aid'],
                #hash=response['hash'],

            #)
            # vkapi.photos.edit()
            print('добавлена фотография id=' + str(photo['id']))
            print(photo)
        except Exception as e:
            print(e)


    for photo in glob.glob('*.png'):
        os.remove(photo)



if __name__ == '__main__':
    main()