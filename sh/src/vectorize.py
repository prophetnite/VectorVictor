'''
Created on Nov 15, 2013

@author: erik
'''
from gensim import corpora, models, similarities



strings = file('../../enwiki-articles1.xml').readlines()

stoplist = set('for a of the and to in'.split())
texts = [[word for word in string.lower().split() if word not in stoplist] for string in strings]

all_tokens = sum(texts, [])
tokens_once = set(word for word in set(all_tokens) if all_tokens.count(word) == 1)
texts = [[word for word in text if word not in tokens_once] for text in texts]
print(texts)
dictionary = corpora.Dictionary(texts)
print dictionary.token2id