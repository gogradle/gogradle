package a

import "testing"

func Test_A1_1(t *testing.T){
    if A1(1) {
         t.Log("Passed")
    } else {
         t.Error("Failed")
    }
}

func Test_A1_2(t *testing.T){
    if !A1(2) {
         t.Log("Passed")
    } else {
         t.Error("Failed")
    }
}

