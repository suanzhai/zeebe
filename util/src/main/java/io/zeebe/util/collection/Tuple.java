/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.util.collection;

public class Tuple<L, R>
{
    private L left;
    private R right;

    public Tuple(L left, R right)
    {
        this.right = right;
        this.left = left;
    }

    public R getRight()
    {
        return right;
    }

    public L getLeft()
    {
        return left;
    }

    public void setRight(R right)
    {
        this.right = right;
    }

    public void setLeft(L left)
    {
        this.left = left;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(left);
        builder.append(", ");
        builder.append(right);
        builder.append(">");
        return builder.toString();
    }

}
