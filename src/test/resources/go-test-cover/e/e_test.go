package e

import "testing"

func Test_Parameterized(t *testing.T) {
    data := []struct {
        name string
        value int32
    }{
        {
            name: "success1",
            value: 1,
        },
        {
            name: "/success2",
            value: 2,
        },
        {
            name: ">success3",
            value: 3,
        },
    }

    for n:= range data {
        index := n
        t.Run(data[index].name, func(t *testing.T) {
            if(data[index].value > 3) {
                t.Fail()
            }
        })
    }
}