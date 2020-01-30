import lbl_mp as lbl
import gensim

fname = "../SRL_DOC_test_tmp.txt"
sentences = gensim.models.word2vec.LineSentence(fname)

model = lbl.LBL()
model.load(name = "lbl.hdf5")
model.prepare_vocabulary(sentences)
model.perplexity(sentences)
