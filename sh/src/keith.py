'''
erik van till
'''
import random
import gensim
print '...'
model = gensim.models.Word2Vec.load('../vecmodel-partial-400.bin')
#model.create_binary_tree()
#print(model.similarity(converted[0],converted[1]))
for i in range(100):
    try:
        print(model.index2word[i].decode('utf-8'))
    except:
        pass
sent = gensim.utils.simple_preprocess(u"pharmaceutical requirements document")
w_last = sent[0]
w_cur = sent[1]
for i in range(2, 32):
    sent.append(str(model.most_similar(positive=[w_cur, sent[i]], negative=[w_last], topn=1)[0][0]))
    w_last = w_cur
    w_cur = sent[i]

print(sent)
