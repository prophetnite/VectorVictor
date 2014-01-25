'''
Created on Jan 11, 2014

@author: Erik
'''

import gensim, bz2


wiki = file('../../enwiki-articles1.xml') #wikipedia article dump
print 'hold on...'

print 'here we go...'
model = gensim.models.Word2Vec(size=1000, window=5, min_count=5, workers=4)

tokens = []
documents = gensim.corpora.wikicorpus._extract_pages(wiki)
for document in documents:
    sentence = gensim.corpora.wikicorpus.filter_wiki(str(document))
    tokens.append(gensim.corpora.wikicorpus.tokenize(sentence.encode('utf-8')))
print(tokens[15])
model.build_vocab(tokens)
model.train(tokens)

print 'model built'
try:
    model.save('../vecmodel-full1000.txt')
    print('model saved')
except:
    print("vecmodel-full1000.txt not saved")
#try:
#    model.save_word2vec_format('../enwiki-articles-word2vec-fmt.bin', binary=True)
#except:
#    print("model bin not saved")


