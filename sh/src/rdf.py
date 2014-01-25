import json
import urllib
from rdflib.graph import ConjunctiveGraph
import gzip
file = gzip.GzipFile('../../freebase-rdf-2014.gz')
api_key = "AIzaSyCIolPNxjOUjE1hbgLct2WP5jS3H-TJVQQ"
service_url = 'https://www.googleapis.com/freebase/v1/rdf'
topic_id = '/music/02h40lc'
params = {
  'key': api_key
}
#url = service_url + topic_id + '?' + urllib.urlencode(params)
g = ConjunctiveGraph()
g.load(file, format="n3")
print(g[6])#so fly
#for s, p, o in g:
#  print s, p, o
