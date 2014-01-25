'''
Created on Nov 8, 2013

@author: erik
'''
import gensim
print('test01')
model = gensim.models.Word2Vec.load('../vecmodel-full1000.bin')

converted = []
results=u'man queen woman bear'.split()
for result in results:
    #result = list(result) # make a list from the tuple
    result = str(result)
    converted.append(result) # tuplify again
print(results)
#try:
    #print(model.syn0[model.vocab.get(converted[2].index)])
#except:
#    pass
#print(format(u'result of: %s + $s - %s' + results))
print(model.most_similar(positive=[converted[0], converted[1]], negative=[converted[2]]))
print(model.doesnt_match(str(u"wombat echidna kangaroo steve").split()))
print(model.similarity(results[2], results[3]))
print(model.similarity(u'woman', u'female'))

#model.create_binary_tree()
#for i in range(10):
#    print(model.index2word[i])
#print(model.similarity(converted[0],converted[1]))
