#import json
#import urllib
#from rdflib.graph import ConjunctiveGraph
#from rdflib import sparqlwrapper
import gzip
import rdflib
from SPARQLWrapper import SPARQLWrapper, JSON

f = gzip.GzipFile('../rdf/content.rdf.u8.gz')#let f be any rdf source you want!

#here's the way to do it networked, but pick a better schema addr
#queryString = """
#PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#>
#PREFIX  dc:     <http://purl.org/dc/elements/1.1/>
#PREFIX  :       <.>
#
#SELECT *
#{
#    { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } }
#}
#"""
#sparql = SPARQLWrapper("http://www.w3.org/2001/XMLSchema")
# add a default graph, though that can also be part of the query string
#sparql.addDefaultGraph(f.read())
#sparql.setQuery(queryString)
#ret = sparql.query()

g = rdflib.Graph()
g = g.parse(data=f.read(),format='n3')
for label in list(g[:rdflib.RDFS.label]) :# all label triples
    print label
#print list(g[::rdflib.Literal('Wolfgang Winkler')]) # all triples with literal string as object
#print list(g[:rdflib.RDFS.object])#all objects (i hope)
#for s, p, o in g:
#     print(o)
#print(ret)
"""
        Combined with SPARQL paths, more complex queries can be
        written concisely:

        Name of all Bobs friends:

        g[bob : FOAF.knows/FOAF.name ]

        Some label for Bob:

        g[bob : DC.title|FOAF.name|RDFS.label]

        All friends and friends of friends of Bob

        g[bob : FOAF.knows * '+']

        etc.

        .. versionadded:: 4.0

        """
