// SPDX-FileCopyrightText: 2023 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

namespace TestProject1;

public class Tests
{
    [SetUp]
    public void Setup()
    {
    }

    [Test]
    public void Test1()
    {
        Console.WriteLine("file:///T:/Temp/file.cmd#select");
        Assert.Pass();
    }
}