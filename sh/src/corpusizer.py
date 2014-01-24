'''
Created on Jan 11, 2014

@author: Erik
'''

import gensim


wiki = file('../../enwiki-articles1.xml') #wikipedia latest full article dump
print 'hold on...'

print 'here we go...'
model = gensim.models.Word2Vec(size=1000, window=5, min_count=2, workers=4)

documents = gensim.corpora.wikicorpus._extract_pages(wiki)
sentences = []
for document in documents:
    #document = gensim.corpora.wikicorpus.filter_wiki(str(documents).encode('utf-8'))
    sentences += gensim.corpora.wikicorpus.tokenize(str(document).encode('utf-8'))

model.build_vocab(list(sentences))

#print 'vocabulary built'
#wiki = file('../../enwiki-articles1.xml') # reopen the file
#sentences = gensim.corpora.wikicorpus.filter_wiki(str(documents).encode('utf-8'))
model.train(sentences)

print 'model trained'

model.save('../vecmodel.bin', binary = True)
model.save('../vecmodel.txt', binary = False)
#model.save_word2vec_format('../enwiki-articles-word2vec-fmt.txt')
print 'model saved'

