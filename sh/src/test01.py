'''
Created on Nov 8, 2013

@author: erik
'''
import gensim

#model = gensim.models.Word2Vec.load('vecmodel.bin', binary=True)
model = gensim.models.Word2Vec.load('vecmodel.txt')

#model.save('/vec.model')
# store the learned weights, in a format the original C tool understands
#model.save_word2vec_format('../vec.model.bin', binary=True)
# or, import word weights created by the (faster) C word2vec
# this way, you can switch between the C/Python toolkits easily
#model = gensim.models.Word2Vec.load_word2vec_format('../vecmodel.bin', binary=True)

converted = []

results='woman queen man'
result = list()
for result in results:
    #result = list(result) # make a list from the tuple
    result = str(unicode(result, 'utf-8'))
    converted.append(result) # tuplify again


#model.create_binary_tree()
#model.syn0[model.vocab.get(converted[2].index)
#print(model.most_similar(positive=[converted[0], converted[1]], negative=[converted[2]])
#print(model.doesnt_match(list("breakfast origami dinner lunch".split())))

print(model.similarity(converted[0], converted[1]))

#print(model['computer'])  # raw numpy vector of a word

