#include <stdio.h>
#include <stdlib.h>

void print(char* str) {
    printf("%s", str);
}

void println(char* str) {
    printf("%s\n", str);
}

void printInt(int n) {
    printf("%d", n);
}

void printlnInt(int n) {
    printf("%d\n", n);
}

char* getString() {
    char* buffer = (char*)malloc(sizeof(char) * 257);
    scanf("%s", buffer);
    return buffer;
}

int getInt() {
    int n;
    scanf("%d", &n);
    return n;
}

char* toString(int i) {
    if (i == 0) {
        char* res = (char*)malloc(sizeof(char) * 2);
        res[0] = '0';
        res[1] = '\0';
        return res;
    }

    char s[10];
    char neg, len = 0;
    if (i > 0)
        neg = 0;
    else {
        neg = 1;
        i = -i;
    }
    while (i > 0) {
        s[len++] = i % 10;
        i /= 10;
    }

    char* res = (char*)malloc(sizeof(char) * (neg + len + 1));
    if (neg > 0)
        res[0] = '-';
    char p = 0;
    while (p < len) {
        res[p + neg] = s[len - p - 1] + '0';
        p++;
    }
    res[len + neg] = '\0';
    return res;
}

char* __string_concatenate(char* str1, char* str2) {
    int len1 = 0, len2 = 0;
    while (str1[len1] != '\0')
        len1++;
    while (str2[len2] != '\0')
        len2++;

    char* res = (char*)malloc(sizeof(char) * (len1 + len2 + 1));
    int len = 0, p;
    p = 0;
    while (p < len1)
        res[len++] = str1[p++];
    p = 0;
    while (p < len2)
        res[len++] = str2[p++];
    res[len] = '\0';
    return res;
}

char __string_equal(char* str1, char* str2) {
    int p = 0;
    while (str1[p] != '\0' && str2[p] != '\0') {
        if (str1[p] != str2[p])
            return 0;
        p++;
    }
    return str1[p] == str2[p] ? 1 : 0;
}

char __string_notEqual(char* str1, char* str2) {
    int p = 0;
    while (str1[p] != '\0' && str2[p] != '\0') {
        if (str1[p] != str2[p])
            return 1;
        p++;
    }
    return str1[p] == str2[p] ? 0 : 1;
}

char __string_lessThan(char* str1, char* str2) {
    int p = 0;
    while (str1[p] != '\0' && str2[p] != '\0') {
        if (str1[p] < str2[p])
            return 1;
        else if (str1[p] > str2[p])
            return 0;
        p++;
    }
    if (str1[p] != '\0')
        return 0;
    else if (str2[p] != '\0')
        return 1;
    else
        return 0;
}

char __string_greaterThan(char* str1, char* str2) {
    int p = 0;
    while (str1[p] != '\0' && str2[p] != '\0') {
        if (str1[p] < str2[p])
            return 0;
        else if (str1[p] > str2[p])
            return 1;
        p++;
    }
    if (str1[p] != '\0')
        return 1;
    else if (str2[p] != '\0')
        return 0;
    else
        return 0;
}

char __string_lessEqual(char* str1, char* str2) {
    int p = 0;
    while (str1[p] != '\0' && str2[p] != '\0') {
        if (str1[p] < str2[p])
            return 1;
        else if (str1[p] > str2[p])
            return 0;
        p++;
    }
    if (str1[p] != '\0')
        return 0;
    else if (str2[p] != '\0')
        return 1;
    else
        return 1;
}

char __string_greaterEqual(char* str1, char* str2) {
    int p = 0;
    while (str1[p] != '\0' && str2[p] != '\0') {
        if (str1[p] < str2[p])
            return 0;
        else if (str1[p] > str2[p])
            return 1;
        p++;
    }
    if (str1[p] != '\0')
        return 1;
    else if (str2[p] != '\0')
        return 0;
    else
        return 1;
}

int __string_length(char* str) {
    int len = 0;
    while (str[len] != '\0')
        len++;
    return len;
}

char* __string_substring(char* str, int left, int right) {
    int len = right - left;
    char* res = (char*)malloc(sizeof(char) * (len + 1));
    int p = 0;
    while (p < len) {
        res[p] = str[left + p];
        p++;
    }
    res[len] = '\0';
    return res;
}

int __string_parseInt(char* str) {
    int res = 0, p = 0;
    while (str[p] != '\0' && str[p] >= '0' && str[p] <= '9')
        res = res * 10 + str[p++] - '0';
    return res;
}

int __string_ord(char* str, int pos) {
    return str[pos];
}

int __array_size(char* arr) {
    return *(((int*)arr) - 1);
}
