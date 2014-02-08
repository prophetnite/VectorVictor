import gensim
model = gensim.models.Word2Vec.load('../vecmodel-full1000.bin')
print model.doesnt_match(u'peach plum pear handbag'.split())
