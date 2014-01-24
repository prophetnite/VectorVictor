import gensim

#model = gensim.models.Word2Vec.load('vecmodel.bin', binary=True)
model = gensim.models.Word2Vec.load('vecmodel.txt', binary=False)
d2Vec.load_word2vec_format('../vecmodel.bin', binary=True)

converted = []

results=['woman','king','man']
result = list()
for result in results:
    result = list(result) # make a list from the tuple
for i in range(len(result)):
    result[i] = str(unicode(result[i], 'utf-8'))
    converted.append(tuple(result)) # tuplify again


default = 'man'
model.create_binary_tree(converted)